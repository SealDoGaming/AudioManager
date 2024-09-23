import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;

class TransferableFile implements Transferable {

    protected static final DataFlavor fileFlavor =
            new DataFlavor(File.class, "A File Object");

    protected static final DataFlavor[] supportedFlavors = {
    		fileFlavor,
            DataFlavor.stringFlavor,
    };

    private final File file;
    public TransferableFile(File file) {

        this.file = file;
    }

    public DataFlavor[] getTransferDataFlavors() {

        return supportedFlavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
    	System.out.println("Ramen");
        return flavor.equals(fileFlavor) ||
                flavor.equals(DataFlavor.stringFlavor);
    }


    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException {

        if (flavor.equals(fileFlavor)) {
            return file;
        } else if (flavor.equals(DataFlavor.stringFlavor)) {
            return file.toString();
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}