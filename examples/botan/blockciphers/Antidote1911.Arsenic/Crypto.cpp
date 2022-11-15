/*
 * This file is part of Arsenic.
 *
 * Copyright (C) 2017 Corraire Fabrice <antidote1911@gmail.com>
 *
 * Arsenic is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Arsenic is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Arsenic. If not, see <http://www.gnu.org/licenses/>.
 */

#include "Crypto.h"
#include <QtCore>
#include <iostream>
#include <fstream>
#include <QString>
#include <QFileInfo>
#include <QDir>
#include <QFile>
#include <vector>
#include <QStringRef>
#include <QStringBuilder>
#include "argonhash.h"
#include "../preferences/Constants.h"

#if defined(Q_OS_LINUX)
#if defined(__clang__)
#include "botan/clang/botan_all.h"
#elif defined(__GNUC__) || defined(__GNUG__)
#include "botan/gcc/botan_all.h"
#endif
#endif

#if defined(Q_OS_WIN64)
#include "botan/msvc_x64/botan_all.h"
#endif


using namespace std;
using namespace Botan;

Crypto_Thread::Crypto_Thread(QObject *parent) :
    QThread(parent)
{
}


void Crypto_Thread::run()
{
    for (auto& inputFileName : m_filenames)
    {
        aborted=false;
        if (cancel)
        {
            aborted=true;
            Crypto_Thread::terminate();
        }

        if (m_encryptBool == true)

        {
            try
            {
                emit PercentProgressChanged(inputFileName,0,"crypto_object");

                encryptFile(inputFileName, m_password, m_algo, m_encoding, m_version);
                if (aborted)
                {   // Reset abort flag
                    aborted=false;
                    emit statusMessage("Error:",inputFileName, "aborted....");

                }


            }
            catch (const Botan::Stream_IO_Error&)
            {
                emit statusMessage("Error: ","Botan::Stream_IO_Error",inputFileName);

            }
            catch (const std::exception& e)
            {
                const auto error = QString{e.what()};
                emit statusMessage("Error: ",error,inputFileName);

            }

        }


        else
        {
            try
            {
                emit PercentProgressChanged(inputFileName,0,"crypto_object");
                decryptFile(inputFileName, m_password, m_version);
                if (aborted)
                {   // Reset abort flag
                    aborted=false;
                    emit statusMessage("Error: ",inputFileName, "Aborted by user: The incomplete decrypted file is deleted.");
                }

            }
            catch (const Botan::Decoding_Error&)
            {
                emit statusMessage("Error: ","Can't decrypt file. Wrong password entered or the file has been corrupted.",inputFileName);
                QFile::remove(outfileresult);

            }
            catch (const Botan::Integrity_Failure&)
            {
                emit statusMessage("Error: ","Can't decrypt file. Wrong password.",inputFileName);
                QFile::remove(outfileresult);

            }
            catch (const Botan::Invalid_Argument&)
            {
                emit statusMessage("Error: ","Can't decrypt file. Is it an encrypted file ?",inputFileName);
                QFile::remove(outfileresult);

            }
            catch (const std::invalid_argument&)
            {
                emit statusMessage("Error: ","Can't decrypt file. Is it an encrypted file ?",inputFileName);
                QFile::remove(outfileresult);

            }
            catch (const std::runtime_error&)
            {
                emit statusMessage("Error: ","Can't read file.",inputFileName);

            }
            catch (const std::exception& e)
            {
                const auto error = QString{e.what()};
                emit statusMessage("Error: ",error,inputFileName);

            }

        }

        if (m_deletefile==true)
        {
            QFile::remove(inputFileName);
        }
    }

}

void Crypto_Thread::setParam(QStringList filenames, QString password, QString algo, bool encryptBool, QString encoding, bool deletefile, QString version)
{
    m_filenames   = filenames;
    m_password    = password;
    m_algo        = algo;
    m_encryptBool = encryptBool;
    m_encoding    = encoding;
    m_deletefile  = deletefile;
    m_version     = version;
}

QString Crypto_Thread::removeExtension(const QString& fileName, const QString& extension)
{
    QFileInfo file{fileName};
    QString newFileName = fileName;

    if (file.suffix() == extension)
    {
        newFileName = file.absolutePath() % QDir::separator() %
                      file.completeBaseName();
    }

    return newFileName;
}

QString Crypto_Thread::uniqueFileName(const QString& fileName)
{
    QFileInfo originalFile{fileName};
    QString uniqueFileName = fileName;

    auto foundUniqueFileName = false;
    auto i = 0;

    while (!foundUniqueFileName && i < 100000)
    {
        QFileInfo uniqueFile{uniqueFileName};

        if (uniqueFile.exists() && uniqueFile.isFile())
        {   // Write number of copies before file extension
            uniqueFileName = originalFile.absolutePath() % QDir::separator() %
                             originalFile.baseName() % QString{" (%1)"} .arg(i + 2);

            if (!originalFile.completeSuffix().isEmpty())
            {   // Add the file extension if there is one
                uniqueFileName += QStringLiteral(".") % originalFile.completeSuffix();
            }

            ++i;
        }
        else
        {
            foundUniqueFileName = true;
        }
    }
    return uniqueFileName;
}

void Crypto_Thread::encryptFile(QString& inputFileName, QString& passphrase, QString& algo, QString& encoding, QString version)
{
    const string algorithmName = algo.toStdString();
    QFileInfo fileInfo{inputFileName};

    if (fileInfo.exists() && fileInfo.isFile() && fileInfo.isReadable())
    {
        Botan::AutoSeeded_RNG rng{};

        const size_t CIPHER_KEY_LEN = 32;  //32 bytes = 256 bits
        const size_t PBKDF_SALT_LEN = 16;  //16 bytes = 128 bits

        // Chachapoly1305 need a 64 bits IV.
        size_t CIPHER_IV_LEN;
        if (algorithmName=="ChaCha20Poly1305")
        {
            CIPHER_IV_LEN = 8;   //8 bytes = 96 bits
        }
        else
        {
            CIPHER_IV_LEN = 16;   //16 bytes = 128 bits
        }

        // Randomize the 16 bytes salt
        vector<uint8_t> pbkdf_salt(PBKDF_SALT_LEN);
        rng.randomize(pbkdf_salt.data(), pbkdf_salt.size());

        // Calculate Argon2 derivation of the password
        const size_t ARGON_OUTPUT_LEN = CIPHER_KEY_LEN + CIPHER_IV_LEN;

        string salt2(pbkdf_salt.begin(), pbkdf_salt.end());
        const SymmetricKey master_key = pwdHashRaw(ARs::T_COST,ARs::M_COST,ARs::PARALLELISM,passphrase.toStdString(),salt2,ARGON_OUTPUT_LEN);

        // Split master_key in two parts. One for cipher_key, one for iv
        const uint8_t* mk = master_key.begin();
        const SymmetricKey cipher_key(mk, CIPHER_KEY_LEN);
        const InitializationVector iv(&mk[CIPHER_KEY_LEN], CIPHER_IV_LEN);

        // Open input for read.and output file. Add .ars extension to the output file
        std::ifstream in{inputFileName.toStdString(), std::ios::binary};

        // Create/open output file with .ars extension
        const QString outputFileName = inputFileName % QStringLiteral(".ars");
        std::ofstream out{outputFileName.toStdString(), std::ios::binary};

        // Out format is:
        // - header
        // - algo name
        // - encoding
        // - b64 encoded Salt

        Botan::Pipe pipe{};

        pipe.append(Botan::get_cipher(algorithmName,
                                      cipher_key,
                                      iv,
                                      Botan::ENCRYPTION));


        if (encoding=="Base64_Encoder")
        {
            pipe.append(new Base64_Encoder(true));
        }

        if (encoding=="Hex_Encoder")
        {
            pipe.append(new Hex_Encoder(true));
        }

        auto headerText = "-------- ENCRYPTED ARSENIC " + version.toStdString() + " FILE --------";

        out << headerText                                              << endl;
        out << algorithmName                                           << endl;
        out << encoding.toStdString()                                  << endl;
        out << Botan::base64_encode(&pbkdf_salt[0], pbkdf_salt.size()) << endl;

        pipe.append(new Botan::DataSink_Stream{out});


        executeCipher(inputFileName, pipe, in, out, outputFileName);

    }

    // Encryption success message if not aborted
    if (aborted != true && cancel != true)
    {
        emit statusMessage(algo,"Sucessfully Encrypted.",inputFileName);
        emit PercentProgressChanged(inputFileName,100,"crypto_object");
    }
}

void Crypto_Thread::decryptFile(const QString& inputFileName,
                                const QString& passphrase, QString version)
{
    const QFileInfo fileInfo{inputFileName};

    if (fileInfo.exists() && fileInfo.isFile() && fileInfo.isReadable())
    {

        std::ifstream in{inputFileName.toStdString(), std::ios::binary};

        // Read the header, algo, encodind and salt from file.
        std::string headerStringStd, algorithmNameStd, pbkdfSaltString,encodingStd;

        std::getline(in, headerStringStd);
        std::getline(in, algorithmNameStd);
        std::getline(in, encodingStd);
        std::getline(in, pbkdfSaltString);

        auto headerString = QString{headerStringStd.c_str()};

        if (headerString != "-------- ENCRYPTED ARSENIC " + version + " FILE --------")
        {
            emit statusMessage("Error: ","Incorrect Header. This is a Arsenic" + version + "file ?",inputFileName);
            return;
        }

        // Chachapoly1305 need a 64 bits IV.
        size_t CIPHER_IV_LEN;
        if (algorithmNameStd=="ChaCha20Poly1305")
        {
            CIPHER_IV_LEN = 8;    //8 bytes = 64 bits
        }
        else
        {
            CIPHER_IV_LEN = 16;   //16 bytes = 128 bits
        }

        const size_t CIPHER_KEY_LEN = 32;  //32 bytes = 256 bits

        Botan::secure_vector<uint8_t>salt = Botan::base64_decode( pbkdfSaltString);


        // Calculate Argon2 derivation of the password
        const size_t ARGON_OUTPUT_LEN = CIPHER_KEY_LEN + CIPHER_IV_LEN;
        string salt2(salt.begin(), salt.end());
        SymmetricKey master_key = pwdHashRaw(ARs::T_COST,ARs::M_COST,ARs::PARALLELISM,passphrase.toStdString(),salt2,ARGON_OUTPUT_LEN);

        // Split master_key in two parts. One for cipher_key, one for iv
        const uint8_t* mk = master_key.begin();
        const SymmetricKey cipher_key(mk, CIPHER_KEY_LEN);
        const InitializationVector iv(&mk[CIPHER_KEY_LEN], CIPHER_IV_LEN);

        // Remove the .ars extension if it's in the file name
        const auto outputFileName = removeExtension(inputFileName,
                                    QStringLiteral("ars"));

        // Create a unique file name for the file in this directory
        auto uniqueOutputFileName = uniqueFileName(outputFileName);
        outfileresult=uniqueOutputFileName;
        std::ofstream out{uniqueOutputFileName.toStdString(), std::ios::binary};

        Botan::Pipe pipe{};

        if (encodingStd=="Base64_Encoder")
        {
            pipe.append(new Base64_Decoder);
        }

        if (encodingStd=="Hex_Encoder")
        {
            pipe.append(new Hex_Decoder);
        }

        pipe.append(Botan::get_cipher(algorithmNameStd,
                                      cipher_key,
                                      iv,
                                      Botan::DECRYPTION));



        pipe.append(new Botan::DataSink_Stream{out});

        executeCipher(inputFileName, pipe, in, out, uniqueOutputFileName);

        // Encryption success message if not aborted
        if (aborted != true && cancel != true)
        {
            emit statusMessage(QString::fromStdString(algorithmNameStd),"Sucessfully Decrypted.",inputFileName);
            emit PercentProgressChanged(inputFileName,100,"crypto_object");
        }

    }

}

void Crypto_Thread::executeCipher(const QString& inputFileName,
                                  Botan::Pipe& pipe,
                                  std::ifstream& in,
                                  std::ofstream& out, QString outfilename)
{


    // Define a size for the buffer vector
    const auto bufferSize = static_cast<std::size_t>(4096);
    Botan::secure_vector<Botan::byte> buffer;
    buffer.resize(bufferSize);

    // Get file size for percent progress calculation
    QFileInfo file{inputFileName};
    const qint64 size = file.size();
    size_t fileindex = 0;
    qint64 percent = -1;

    pipe.start_msg();

    while (in.good() && aborted==false && cancel==false)
    {

        in.read(reinterpret_cast<char*>(&buffer[0]), static_cast<streamsize>(buffer.size()));
        size_t remainingSize = static_cast<size_t>(in.gcount());
        pipe.write(&buffer[0], remainingSize);

        // Calculate progress in percent
        fileindex += remainingSize;
        const auto nextFraction = static_cast<double>(fileindex) /
                                  static_cast<double>(size);
        const qint64 nextPercent = static_cast<qint64>(nextFraction * 100);

        if (nextPercent > percent && nextPercent < 100)
        {
            percent = nextPercent;
            emit PercentProgressChanged(inputFileName,percent,"crypto_object");
        }

        if (in.eof())
        {
            pipe.end_msg();
        }

        while (pipe.remaining() > 0)
        {
            const auto buffered = pipe.read(&buffer[0], buffer.size());
            out.write(reinterpret_cast<const char*>(&buffer[0]), static_cast<streamsize>(buffered));
        }

    }

    if (aborted || cancel)
        QFile::remove(outfilename);


    if (in.bad() || (in.fail() && !in.eof()))
    {
        QFile::remove(outfilename);
        emit statusMessage("IO Error: ", "Can't read file.", inputFileName);

    }

    out.flush();

}
