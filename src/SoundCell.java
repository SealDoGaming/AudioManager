import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class SoundCell extends JPanel implements ActionListener,ChangeListener,LineListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int WIDTH = 250;
	public static final int HEIGHT = 100;
	
	public static final int VOLUME_MIN = 3;
	public static final int VOLUME_MAX = 131;
	public static final int VOLUME_INIT = 100;
	
	private final boolean repeat = false;
	
	protected String soundName="";
	protected File soundFile = null;
	
	
	private JLabel soundLabel;
	protected JLabel lengthLabel = new JLabel();
	protected JButton playButton;
	
	private JSlider volumeSlider;
	private JSlider timeSlider;
	protected Timer timeUpdater;
	private Timer delayedUpdate;
	private int currentFrame;
	
	
    protected boolean ignoreStateChange;
	
	protected ClipPlayer soundPlayer;
	
	
	public SoundCell() {
		this("");
		
	}
	public SoundCell(String filename) {
		super();
		setupSize();
		
		if(!filename.isEmpty()) {
			
			File file = new File(filename);
			newSound(file);
			soundFile = file;
			soundName = file.getName();
			
		}
		
		setupGUI();
		checkVisibility();
		endStep();
		
	}
	public SoundCell(File file) {
		super();
		soundName = file.getName();

		System.out.println(file);
		newSound(file);
		soundFile = file;
		
		setupSize();
		setupGUI();
		checkVisibility();
		endStep();
		
		
	}
	protected void endStep() {
		var ds = new DragSource();
        ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, AudioManager.currentManager);
		
	}
	
	
	protected void setupSize() {
		this.setPreferredSize(new Dimension(SoundCell.WIDTH,SoundCell.HEIGHT));
		this.setMinimumSize(getPreferredSize());
		this.setMaximumSize(getPreferredSize());
		this.setBorder(BorderFactory.createLineBorder(Color.black));
	}
	private void setupGUI() {
		this.setLayout(new BorderLayout());
		
		JPanel contentBox = new JPanel();
		contentBox.setLayout(new BorderLayout());
		
		JPanel sliderBox = new JPanel();
		
		JPanel upper = new JPanel();
		JPanel mid = new JPanel();
		JPanel lower = new JPanel();
		lower.setLayout(new BoxLayout(lower,BoxLayout.X_AXIS));
		
		
		soundLabel = new JLabel(soundName);
		soundLabel.setAlignmentX(CENTER_ALIGNMENT);
		soundLabel.setAlignmentY(CENTER_ALIGNMENT);
		upper.add(soundLabel);
		
		playButton = new JButton("Play");
		
		playButton.addActionListener(this);  
		

		volumeSlider = new JSlider(JSlider.VERTICAL,SoundCell.VOLUME_MIN,SoundCell.VOLUME_MAX, SoundCell.VOLUME_INIT);
		volumeSlider.setPreferredSize(new Dimension(volumeSlider.getPreferredSize().width*2, SoundCell.HEIGHT));
		volumeSlider.addChangeListener(this);

		volumeSlider.setMajorTickSpacing(100);
		volumeSlider.setMinorTickSpacing(100);
		
		volumeSlider.setPaintTicks(true);
		
		
		timeSlider = new JSlider(JSlider.HORIZONTAL,0,1, 0);
		
		lower.add(Box.createHorizontalGlue());
		lower.add(timeSlider);
		lower.add(Box.createHorizontalGlue());
		addTimeLabel(lower);
		lower.add(playButton);
		lower.add(Box.createHorizontalGlue());
		//lower.setBackground(Color.red);
		
		contentBox.add(upper,BorderLayout.PAGE_START);
		contentBox.add(mid,BorderLayout.CENTER);
		contentBox.add(lower,BorderLayout.PAGE_END);
		
		sliderBox.add(volumeSlider);
		
		this.add(contentBox,BorderLayout.CENTER);
		this.add(sliderBox,BorderLayout.LINE_END);
		
		//System.out.println(volumeSlider.getPreferredSize());
	}
	public void addTimeLabel(JPanel lower) {
		lengthLabel = new JLabel("0:00 - 0:00");
	}
	public String timeString(long microseconds) {
		return timeString(microseconds,false);
	}
	public String timeString(long microseconds,boolean addMinimum) {

		int totalSeconds = (int) Math.round(((double)microseconds)/1000000);
		int minutes = totalSeconds/60;
		int seconds = totalSeconds%60;
		int digitOne = seconds/10;
		int digitTwo = seconds%10;
		return (minutes+":"+digitOne+""+digitTwo );
	}

	public void setupTimeSlider() {
		timeSlider.setMaximum((int) soundPlayer.getFrameLength());
		timeSlider.setValue(0);
		soundPlayer.setCurrentFrame(0);
		updateTimeSlider();
		
		timeUpdater = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	updateTimeSlider();

            }
            
        });
		delayedUpdate = new Timer(150, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int frame = timeSlider.getValue();
                soundPlayer.setCurrentFrame(frame);

                System.out.println("Current frame: " + frame);


            }
        });
		delayedUpdate.setRepeats(false);
        timeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
            	System.out.println(ignoreStateChange);
                if (ignoreStateChange) {
                    return;
                }
                delayedUpdate.restart();
            }
        });
	}
	public void updateTimeSlider() {

    	ignoreStateChange = true;
        currentFrame = soundPlayer.getCurrentFrame();
    	timeSlider.setValue(currentFrame);

		long soundLength = soundPlayer.getMicrosecondLength();
		long soundPosition = soundPlayer.getMicrosecondPosition();
		soundPosition %= soundLength;
		
		lengthLabel.setText(timeString(soundPosition)+" - "+timeString(soundLength,true));
    	//System.out.println("Frame: "+currentFrame);
        ignoreStateChange = false;
	}
	
	public void checkVisibility() {
		soundLabel.setText(soundName);
		boolean visible = !soundName.isEmpty();
		soundLabel.setVisible(visible);
		lengthLabel.setVisible(visible);
		timeSlider.setVisible(visible);
		playButton.setVisible(visible);
		volumeSlider.setVisible(visible);
		if(soundPlayer!=null) {
			setupTimeSlider();
			volumeSlider.setValue(100);
		}
	}
	
	public void stopPrevious() {
		if(soundPlayer!=null) {
			try {
				soundPlayer.stop();
				
			} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(timeUpdater!=null&&delayedUpdate!=null) {
			timeUpdater.stop();
			
			delayedUpdate.stop();
		}
	}
	public void reset() {
		stopPrevious();
		soundName="";
		soundFile=null;
		checkVisibility();
		
	}
	
	
	public void newSound(File file) {
		stopPrevious();
		soundName = file.getName();
		soundFile = file;
		soundPlayer = new ClipPlayer(file,repeat);
		soundPlayer.addListener(this);
	}
	public void close() {
		soundPlayer.closeAudio();
		Container parent = this.getParent();
		SoundCell copy = this;

		SwingUtilities.invokeLater(new Runnable(){
    		public void run() {
    			parent.remove(copy);
    			parent.revalidate();
    			parent.repaint();
    		}
        });
		
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e){
		if(!soundPlayer.isPlaying()) {
			try {
				soundPlayer.resume();
			} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    }else {
	    	soundPlayer.pause();
	    }
		
	}
	
	public void stateChanged(ChangeEvent e) {
	    JSlider source = (JSlider)e.getSource();
	    if(source.equals(volumeSlider)) {
	    	float volume = ((float)source.getValue())/100f;
		    if (!source.getValueIsAdjusting()) {
		        soundPlayer.setVolume(volume);
		    }
	    }else {
	    	System.out.println("Pants");
	    }
	    
	}
	public void update(LineEvent event) {
		//System.out.println(event.getType());
		if(event.getType().equals(LineEvent.Type.STOP)) {
	    	playButton.setText("Play");
			timeUpdater.stop();
	    	if(soundPlayer.getCurrentFrame()==soundPlayer.getFrameLength()&&!repeat) {
	    		soundPlayer.setCurrentFrame(0);
	    	}
    		updateTimeSlider();
		}else if(event.getType().equals(LineEvent.Type.START)) {
	    	playButton.setText("STOP");
	    	timeUpdater.start();
	    	
		}
	}
	public boolean getRepeat() {
		return repeat;
	}
	public String getSoundName() {
		return soundName;
	}
	public boolean isName(String name) {
		return soundName.equals(name);
	}
	public File getSoundFile() {
		//System.out.println(soundFile);
		return soundFile;
	}
	public void cellStatusCheck() {
		System.out.println("Name :"+soundName);
		System.out.println("Title Visibility: "+soundLabel.isVisible());
		System.out.println("Length Visibility: "+lengthLabel.isVisible());
		System.out.println("Button Visibility: "+playButton.isVisible());
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
