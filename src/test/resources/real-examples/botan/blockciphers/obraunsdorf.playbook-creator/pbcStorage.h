/** @file pbcStorage.h
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

#ifndef PBCSTORAGE_H
#define PBCSTORAGE_H

#include "pbcSingleton.h"
#include "models/pbcPlay.h"
#include "gui/pbcPlayView.h"
#include <botan/pbkdf.h>
#include <botan/secmem.h>
#include <botan/data_src.h>
#include <string>
#include <vector>
#include <list>

class PBCStorage : public PBCSingleton<PBCStorage> {
    friend class PBCSingleton<PBCStorage>;

private:
    const std::string _CIPHER = "AES-256/GCM";
    const std::string _HASH = "SHA-256";
    const std::string _PBKDF = "PBKDF2(SHA-256)";
    const unsigned int _PBKDF_ITERATIONS = 10000;
    const unsigned int _SALT_SIZE = 16;  // in Bytes
    const unsigned int _IV_SIZE = 12;     // in Bytes = 96 Bits, recommended by BSI (https://www.bsi.bund.de/SharedDocs/Downloads/DE/BSI/Publikationen/TechnischeRichtlinien/TR02102/BSI-TR-02102.pdf?__blob=publicationFile&v=10)
    const unsigned int _KEY_SIZE = 32;  // in Bytes = 256 Bits
    const unsigned int _HASH_SIZE = 32;  // in Bytes = 256 Bits
    const std::string _PREAMBLE = "Playbook-Creator\n"
                           + PBCVersion::getVersionString() + "\n" \
                           "playbook\n";

    std::string _currentPlaybookFileName;
    boost::shared_ptr<Botan::SecureVector<Botan::byte>> _saltSP;
    boost::shared_ptr<Botan::OctetString> _keySP;

    void checkVersion(const std::string &version);

    void generateAndSetKey(const std::string &password);

    void setCryptoKey(Botan::OctetString key,
                      Botan::SecureVector<Botan::byte> salt);

    void encrypt(const std::string &input, std::ofstream &outFile);  // NOLINT
    void decrypt(const std::string &password,
                 std::ostream &ostream,  // NOLINT
                 std::ifstream &inFile); // NOLINT

protected:
    PBCStorage() {}

public:
    void init(const std::string &fileName);

    void savePlaybook(const std::string &password, const std::string &fileName);

    void automaticSavePlaybook();

    void writeToCurrentPlaybookFile();

    void loadPlaybook(const std::string &password, const std::string &fileName);

    void exportPlay(const std::string &fileName, PBCPlaySP play);

    void importPlay(const std::string &fileName, PBCPlaySP play);

    void exportAsPDF(const std::string &fileName,
                     boost::shared_ptr<QStringList> playListSP,
                     const unsigned int paperWidth,
                     const unsigned int paperHeight,
                     const unsigned int columns,
                     const unsigned int rows,
                     const unsigned int marginLeft,
                     const unsigned int marginRight,
                     const unsigned int marginTop,
                     const unsigned int marginBottom);
};

#endif  // PBCSTORAGE_H
