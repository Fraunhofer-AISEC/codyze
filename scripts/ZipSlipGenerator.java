
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipSlipGenerator {

    public static void main(String[] args) {
        File zipSlip = new File("zip-slip.zip");

        String[] zipEntryNames = new String[] {
                "good.mark",
                "../../../../../../evil.mark"
        };

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipSlip))) {
            for (String zipEntryName : zipEntryNames) {
                ZipEntry ze = new ZipEntry(zipEntryName);
                zos.putNextEntry(ze);
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }
}