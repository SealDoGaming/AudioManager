import java.io.File;

import javax.swing.filechooser.FileFilter;

public class AudioFileFilter extends FileFilter {
	
	public final static String[] acceptedTypes = {".mp3",".wav"};
	
	@Override
	public boolean accept(File f) {
		// TODO Auto-generated method stub
		for(String extension:acceptedTypes) {
			if(f.getName().endsWith(extension)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return ".mp3 and .wav files";
	}

}
