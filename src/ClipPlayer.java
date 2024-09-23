import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;



public class ClipPlayer {
	private int currentFrame = 0;
	private Clip audio;
	//private Player player;
	
	private AudioInputStream audioInputStream;
	private AudioFormat mp3Format;
	
	private File sound;
	private boolean playing;
	private boolean repeating;
	private int loopValue;
	
	private LineListener mainListener;
	
	
	private boolean upset = false;
	private boolean lineTactics = false;
	
	private FloatControl volumeControl;
	
	public ClipPlayer(File file,boolean repeat) {
	        // create AudioInputStream object
		repeating = repeat;
		sound = file;
		
		try {
			
			if(upset) {
				throw new UnsupportedAudioFileException();
			}
	        if(lineTactics) {
	        	
	        }else {
	        	System.out.println("Loading: "+sound.getName());
	        	
				//Bitstream bitStream = new Bitstream(new FileInputStream("path/to/audio.mp3"));
			    audioInputStream = AudioSystem.getAudioInputStream(file);
			    
			    
			    //System.out.println(audioInputStream.getFormat());
			    AudioFormat oldFormat = audioInputStream.getFormat();
			        // create audio reference
		        audio = AudioSystem.getClip();
		        if(file.getName().contains(".mp3")) {
		        	
		        	mp3Format = new AudioFormat(oldFormat.getSampleRate(), 16, oldFormat.getChannels(), true, oldFormat.isBigEndian());
		        	audioInputStream = AudioSystem.getAudioInputStream(mp3Format, audioInputStream);
		        	
		        }
		        audio.open(audioInputStream);
		        //audio.open(format,data, offset,bufferSize);
		        volumeControl = (FloatControl) audio.getControl(FloatControl.Type.MASTER_GAIN);
		        volumeControl.setValue(0.0f);
		        

	        	//System.out.println("Maximum Volume: "+volumeControl.getMaximum());
	        	//System.out.println("Minimum Volume: "+volumeControl.getMinimum());
		        
			    if(repeat) {
		        	loopValue= Clip.LOOP_CONTINUOUSLY;
		        }else {
		        	loopValue = 0;
		        }
		        
	        }
			
		        // open audioInputStream to the audio
		}catch(UnsupportedAudioFileException e) {
			//System.out.println("Wrong File Type");
			
			/*
			try {
				
				player = new MP3Player(file);
				player.setRepeat(repeat);
				
				FileInputStream fileInputStream = new FileInputStream(new File(filename).getAbsoluteFile());
	            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
	            player = new Player(bufferedInputStream);
	            
	            mp3 = true;
	            
	           
			}catch(Exception g){
				System.out.println("help");
				System.out.println(g);
				
			}
		*/
		
		}catch(IOException e) {
			System.out.println(e);
		}catch(Exception e) {
			System.out.println("StuffWentWrong");
			System.out.println(e);
		}
		//System.out.println(file);
	}

	
	public void play() {
        //start the audio
		System.out.println(sound.getName());
		audio.setFramePosition(currentFrame);
		audio.loop(loopValue);
		playing = true;
        
    }
	public void pause() {
        if (!isPlaying()) {
            System.out.println("audio is already paused");
            return;
        }
        
		this.currentFrame = this.audio.getFramePosition();
        audio.stop();
        playing = false;
        
    }
	public void resume() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		if (isPlaying()) {
            System.out.println("audio is already playing");
            return;
        }
        audio.setFramePosition(currentFrame);
        this.play();
        playing = true;
		
	}	public void stop() throws UnsupportedAudioFileException,IOException, LineUnavailableException {
		currentFrame = 0;
		audio.setFramePosition(0);
        audio.stop();
        System.out.println("Stop Called");
        //audio.close();
        
        playing = false;
        
    }
	
	
	public boolean isPlaying() {
		boolean playingValue = playing&&!(audio.getFramePosition()==audio.getFrameLength()||audio.getFramePosition()==0);
		//System.out.println(playing+","+(audio.getFramePosition()==audio.getFrameLength()));
		//System.out.println(playingValue);
		return playingValue;
	}
	
	public boolean getRepeat() {
		return repeating;
	}
	public int getFrameLength() {
		
        //int duration = (int)((double) audio.getFrameLength()) /audio.getFormat().getFrameRate();
        int duration = audio.getFrameLength();
        //System.out.println("Length: "+duration);
        return (int) duration;
		
	}
	public int getCurrentFrame() {
		currentFrame = audio.getFramePosition();
		if (repeating) {
			int duration = getFrameLength();
			currentFrame = currentFrame % duration;
		}
		//System.out.println("CurrentFrame: "+currentFrame);
		return currentFrame;
	}
	public void setCurrentFrame(int frame) {
		currentFrame = frame;
		audio.setFramePosition(frame);
	}
	public long getMicrosecondPosition() {
		return audio.getMicrosecondPosition();
	}
	public long getMicrosecondLength() {
		return audio.getMicrosecondLength();
	}

	public void setVolume(float volume) { 
		audio.removeLineListener(mainListener);
        audio.stop();
        volumeControl.setValue(50f * (float) Math.log10(volume));
        if(isPlaying()) {
	        audio.loop(loopValue);
        }
        audio.addLineListener(mainListener);
	}
	
	public void addListener(LineListener listener) {
		mainListener = listener;
		audio.addLineListener(listener);
	}
	public void closeAudio() {
	    try {
			audioInputStream.close();
			audio.close();
			//System.out.println(audio.isOpen());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
