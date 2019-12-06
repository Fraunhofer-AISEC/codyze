/** @file pbcStorage.cpp
    This file is part of Playbook Creator.

    Playbook Creator is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Playbook Creator is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Playbook Creator.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2015 Oliver Braunsdorf

    @author Oliver Braunsdorf
*/

#include "pbcStorage.h"
#include "models/pbcPlaybook.h"
#include "util/pbcConfig.h"
#include "util/pbcExceptions.h"
#include <botan/version.h>
#include <botan/pipe.h>
#include <botan/data_src.h>
#include <botan/data_snk.h>
#include <botan/auto_rng.h>
#include <botan/aead.h>
#include <botan/cipher_mode.h>
#include <botan/cipher_filter.h>
#include <boost/archive/text_oarchive.hpp>
#include <boost/archive/text_iarchive.hpp>
#include <fstream>
#include <istream>
#include <iostream>
#include <string>
#include <QPrinter>
#include <QPainter>
#include "pbcVersion.h"

/**
 * @class PBCStorage
 * @brief PBCStorage is the responsible class for persistent storage of created
 * playbooks.
 *
 * This includes binary export and import in a self defined (encrypted) "*.pbc"
 * file format as well as graphical export of the playbooks in PDF file format.
 */

/**
 * @brief Checks whether the version of a loaded playbook is newer than the
 * application's version
 * @param version The version string of the loaded playbook
 */
void PBCStorage::checkVersion(const std::string &version) {
    // TODO(obr): do better version checking
    int result = PBCVersion::compareCurrentVersionTo(version);
    if(result < 0) {
        throw PBCDeprecatedVersionException("Version of the new playbook is: " + version);  //NOLINT
    }
    pbcAssert(result >= 0);
    pbcAssert(BOTAN_VERSION_CODE >= BOTAN_VERSION_CODE_FOR(1, 10, 9));
}

/**
 * @brief Generates a cryptographic key from a password and a random salt value
 * and set them as the current key and salt
 * @param password The password that the key is derived from
 */
void PBCStorage::generateAndSetKey(const std::string &password) {
    Botan::AutoSeeded_RNG rng;
    Botan::SecureVector<Botan::byte> salt = rng.random_vec(_SALT_SIZE);
    boost::shared_ptr<Botan::PBKDF> pbkdf(Botan::get_pbkdf(_PBKDF));  // NOLINT
    Botan::OctetString key = pbkdf->derive_key(_KEY_SIZE, password,
                                               &salt[0], salt.size(),
                                               _PBKDF_ITERATIONS);
    setCryptoKey(key, salt);
}

/**
 * @brief Resets the current key and salt
 * @param key The new key
 * @param salt The new salt
 */
void PBCStorage::setCryptoKey(Botan::OctetString key,
                        Botan::SecureVector<Botan::byte> salt) {
    _saltSP.reset(new Botan::SecureVector<Botan::byte>(salt));
    _keySP.reset(new Botan::OctetString(key));
}

/**
 * @brief Encrypts the input string and writes the result to a file stream
 * @param input The input string (usually the serialized playbook)
 * @param outFile The output file
 */
void PBCStorage::encrypt(const std::string& input,
                         std::ofstream& outFile) {
    pbcAssert(_keySP != NULL && _saltSP != NULL);
    outFile.write((const char*)_saltSP->data(), _saltSP->size());

    /*Botan::OctetString preambleBytes(_PREAMBLE);
    aead->set_ad(preambleBytes.bits_of());*/


    Botan::AutoSeeded_RNG rng;
    Botan::InitializationVector iv = rng.random_vec(_IV_SIZE); //TODO make constant in header
    outFile.write((const char*)iv.bits_of().data(), iv.bits_of().size());

    Botan::Pipe encryptor(Botan::get_cipher(_CIPHER, *_keySP, iv, Botan::Cipher_Dir::ENCRYPTION),
            new Botan::DataSink_Stream(outFile));
    encryptor.process_msg(input);
}

/**
 * @brief Decrypts the given file using a password and writes the result to
 * an  output stream
 * @param password The password string, which the decryption key is derived from
 * @param ostream The output stream to which the decrypted playbook is written
 * @param inFile The file where the encrypted playbook is stored
 */
void PBCStorage::decrypt(const std::string &password,
                         std::ostream &ostream,
                         std::ifstream &inFile) {
    boost::shared_ptr<Botan::PBKDF> pbkdf(Botan::get_pbkdf(_PBKDF));
    Botan::SecureVector<Botan::byte> salt(_SALT_SIZE);
    inFile.read(reinterpret_cast<char*>(&salt[0]), _SALT_SIZE);
    Botan::SecureVector<Botan::byte> iv(_IV_SIZE);
    inFile.read(reinterpret_cast<char*>(&iv[0]), _IV_SIZE);

    Botan::OctetString key = pbkdf->derive_key(_KEY_SIZE, password,
                                               &salt[0], salt.size(),
                                               _PBKDF_ITERATIONS);

    Botan::Keyed_Filter* cipher = Botan::get_cipher(_CIPHER, key, iv, Botan::Cipher_Dir::DECRYPTION);
    Botan::Pipe decryptor(cipher, new Botan::DataSink_Stream(ostream));

    Botan::DataSource_Stream source(inFile);
    try {
        decryptor.process_msg(source);
    } catch (Botan::Integrity_Failure& e) {
        throw PBCDecryptionException("Error while decrypting playbook. "
                                     "Maybe you entered the wrong password to often "
                                     "or someone tampered the playbook file.");
    }
    /*std::string readPreamble(preambleBytes.begin(), preambleBytes.end());
    std::cout << "read preamble: " << readPreamble << std::endl*/
    setCryptoKey(key, salt);
}

/**
 * @brief Initializes the PBCStorage instance when a new playbook loaded. Hence
 * the key and the salt have to be reset.
 * @param fileName The file name of the new playbook
 */
void PBCStorage::init(const std::string &fileName) {
    _currentPlaybookFileName = fileName;
    _keySP.reset();
    _saltSP.reset();
}

/**
 * @brief Wrapper function which generates key and salt and sets the file name
 * first and the actual saving algorithm is implemented in writeToCurrentPlaybookFile()
 * @param password
 * @param fileName
 */
void PBCStorage::savePlaybook(const std::string& password,
                              const std::string& fileName) {
    generateAndSetKey(password);
    _currentPlaybookFileName = fileName;
    writeToCurrentPlaybookFile();
}

/**
 * @brief Checks if key and salt are set before saving the playbook, so we don't
 * need to enter the password again.
 */
void PBCStorage::automaticSavePlaybook() {
    if(_keySP != NULL && _saltSP != NULL) {
        writeToCurrentPlaybookFile();
    } else {
        throw PBCAutoSaveException("Cryptographic key is missing.");  //NOLINT
    }
}

/**
 * @brief Writes the playbook to a string buffer using Boost serialization
 * framework.
 *
 * Encrypting and writing to file is done via PBCStorage::encrypt() function
 */
void PBCStorage::writeToCurrentPlaybookFile() {
    pbcAssert(_currentPlaybookFileName != "");
    std::string extension = _currentPlaybookFileName.substr(_currentPlaybookFileName.size() - 4);  //NOLINT
    pbcAssert(extension == ".pbc");
    std::stringbuf buff;
    std::ostream ostream(&buff);
    boost::archive::text_oarchive archive(ostream);
    archive << *PBCPlaybook::getInstance();

    std::ofstream ofstream(_currentPlaybookFileName,
                           std::ios_base::out | std::ios_base::binary);

    ofstream << _PREAMBLE;

    try {
        encrypt(buff.str(), ofstream);
    } catch(std::exception& e) {
        ofstream.close();
        // std::remove(fileName.c_str());
        std::cout << e.what() << std::endl;  // TODO(obr): message to user
    }

    ofstream.close();
}

/**
 * @brief Passes a file to the PBCStorage::decrypt() function to decrypt it and
 * write it to a string buffer. The buffer is then read and deserialized to a
 * PBCPlaybook instance using Boost serialization framework
 * @param password The decryption password
 * @param fileName The path to the file where the playbook ist stored
 */
void PBCStorage::loadPlaybook(const std::string &password,
                              const std::string &fileName) {
    std::string extension = fileName.substr(fileName.size() - 4);
    pbcAssert(extension == ".pbc");
    std::stringbuf buff;
    std::ostream ostream(&buff);
    std::ifstream ifstream(fileName, std::ios_base::binary);

    const size_t maxLen = _PREAMBLE.length();
    char* preambleBuffer = new char[maxLen];
    ifstream.getline(preambleBuffer, maxLen);
    std::string pbcString(preambleBuffer);
    pbcAssert(pbcString == "Playbook-Creator");

    ifstream.getline(preambleBuffer, maxLen);
    std::string version(preambleBuffer);
    checkVersion(version);

    ifstream.getline(preambleBuffer, maxLen);
    std::string filetypeString(preambleBuffer);
    pbcAssert(filetypeString == "playbook");

    delete[] preambleBuffer;
    preambleBuffer = NULL;

    try {
        decrypt(password,
            ostream,
            ifstream);
    } catch (PBCDecryptionException& e) {
        throw e;
    } catch(std::exception& e) {
        throw PBCStorageException(e.what());  // TODD(obr): message to user
    }
    _currentPlaybookFileName = fileName;

    std::istream istream(&buff);
    boost::archive::text_iarchive archive(istream);
    archive >> *PBCPlaybook::getInstance();
}


/**
 * @brief Graphically exports the playbook in PDF file format.
 *
 * The plays are arranged in a grid.
 * @param fileName The PDF file to which the playbook is exported
 * @param playViews A list of PBCPlayViews in which the exported plays are
 * graphically rendered using the Qt framework
 * @param paperWidth The width of one page
 * @param paperHeight The height of one page
 * @param columns The number of columns on one page
 * @param rows The number of rows on one page
 * @param marginLeft The left margin
 * @param marginRight The right margin
 * @param marginTop The margin from the top
 * @param marginBottom The margin from the bottom
 */
void PBCStorage::exportAsPDF(const std::string& fileName,
                             boost::shared_ptr<QStringList> playListSP,  //NOLINT
                             const unsigned int paperWidth,
                             const unsigned int paperHeight,
                             const unsigned int columns,
                             const unsigned int rows,
                             const unsigned int marginLeft,
                             const unsigned int marginRight,
                             const unsigned int marginTop,
                             const unsigned int marginBottom) {
    std::string extension = fileName.substr(fileName.size() - 4);
    pbcAssert(extension == ".pdf");
    QPrinter printer(QPrinter::HighResolution);
    printer.setOutputFileName(QString::fromStdString(fileName));
    printer.setOutputFormat(QPrinter::PdfFormat);
    unsigned int autoPaperWidth = paperWidth;
    unsigned int autoPaperHeight = paperHeight;
    if (paperWidth == 0 || paperHeight == 0) {
        // is needed because play views are rendered to pixel graphics,
        // which would result in huge files if not scaled down
        float scaleFactor = 0.025;

        autoPaperWidth = (PBCConfig::getInstance()->canvasWidth() * columns + marginLeft + marginRight) * scaleFactor;
        autoPaperHeight = (PBCConfig::getInstance()->canvasHeight() * rows + marginTop + marginBottom) * scaleFactor;

    }
    std::cout << "paper width = " << autoPaperWidth << "; paper height = " << autoPaperHeight << std::endl;
    printer.setPaperSize(QSizeF(autoPaperWidth, autoPaperHeight), QPrinter::Millimeter);

    printer.setPageMargins(marginLeft,
                           marginTop,
                           marginRight,
                           marginBottom,
                           QPrinter::Millimeter);
    pbcAssert(columns > 0);
    pbcAssert(rows > 0);

    boost::shared_ptr<qreal> pixelMarginLeftSP(new qreal());
    boost::shared_ptr<qreal> pixelMarginRightSP(new qreal());
    boost::shared_ptr<qreal> pixelMarginTopSP(new qreal());
    boost::shared_ptr<qreal> pixelMarginBottomtSP(new qreal());
    printer.getPageMargins(pixelMarginLeftSP.get(),
                           pixelMarginTopSP.get(),
                           pixelMarginRightSP.get(),
                           pixelMarginBottomtSP.get(),
                           QPrinter::DevicePixel);
    QSize playSize = printer.pageRect().size();
    playSize.setWidth(playSize.width() / columns);
    playSize.setHeight(playSize.height() / rows);

    printer.setPageMargins(0.0, 0.0, 0.0, 0.0, QPrinter::Millimeter);
    QPainter painter(&printer);
    bool paintBorder = true;
    if(marginLeft == 0 &&
       marginRight == 0 &&
       marginTop == 0 &&
       marginBottom == 0) {
        paintBorder = false;
    }
    painter.setPen(QPen(QBrush(Qt::red), 20, Qt::DashLine));
    QRectF borderRect(10,
                      10,
                      printer.paperRect().width() - 10,
                      printer.paperRect().height() - 10);
    if(paintBorder == true) {
        painter.drawRect(printer.paperRect());
    }



    unsigned int x = 0;
    unsigned int y = 0;
    unsigned int columnCount = 1;
    unsigned int rowCount = 1;

    unsigned int old_width = PBCConfig::getInstance()->canvasWidth();
    unsigned int old_height = PBCConfig::getInstance()->canvasHeight();
    PBCConfig::getInstance()->setCanvasSize(playSize.width(), playSize.height());

    for(QString playName : *playListSP) {
        PBCPlaySP playSP = PBCPlaybook::getInstance()->getPlay(playName.toStdString()); //NOLINT
        boost::shared_ptr<PBCPlayView> playViewSP(new PBCPlayView(playSP));  //NOLINT
        playViewSP->render(&painter,
                           QRectF(QPointF(x + *pixelMarginLeftSP, y + *pixelMarginTopSP), playSize),  //NOLINT
                           QRectF(),
                           Qt::IgnoreAspectRatio);
        ++columnCount;
        x = x + playSize.width();
        if(columnCount > columns) {
            x = 0;
            y = y + playSize.height();
            ++rowCount;
            columnCount = 1;
        }
        if(rowCount > rows) {
            bool successful = printer.newPage();
            pbcAssert(successful == true);
            if(paintBorder == true) {
                painter.drawRect(borderRect);
            }
            rowCount = 1;
            y = 0;
        }
    }

    PBCConfig::getInstance()->setCanvasSize(old_width, old_height);
}
