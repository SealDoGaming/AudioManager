import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SongCell extends SoundCell {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7310436354422584396L;
	protected final boolean repeat = true;
	
	
	public SongCell() {
		super("");
	}
	public SongCell(String name) {
		super(name);
	}
	public SongCell(File file) {
		super(file);
	}
	@Override
	public void addTimeLabel(JPanel lower) {
		lengthLabel = new JLabel("0:00 - 0:00");
		lower.add(lengthLabel);
		lower.add(Box.createHorizontalGlue());
	}
	@Override
	protected void setupSize() {
		super.setupSize();
		this.setPreferredSize(new Dimension(SoundCell.WIDTH*2,SoundCell.HEIGHT));
		this.setMinimumSize(getPreferredSize());
		this.setMaximumSize(getPreferredSize());
		this.setBorder(BorderFactory.createLineBorder(Color.black));
	}
	@Override
	public void newSound(File file) {
		if(soundPlayer!=null) {
			try {
				soundPlayer.stop();
			} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		soundName = file.getName();
		soundFile = file;
		soundPlayer = new ClipPlayer(file,repeat);
		soundPlayer.addListener(this);
		//System.out.println(super.soundPlayer.getRepeat());
	}
	@Override
	public void update(LineEvent event) {
		System.out.println(event.getType());
		if(event.getType().equals(LineEvent.Type.STOP)) {
	    	playButton.setText("Play");
			timeUpdater.stop();
	    	if(soundPlayer.getCurrentFrame()==soundPlayer.getFrameLength()&&!repeat) {
	    		//soundPlayer.setCurrentFrame(0);
	    		updateTimeSlider();
	    	}
		}else if(event.getType().equals(LineEvent.Type.START)) {
	    	playButton.setText("STOP");
	    	timeUpdater.start();
	    	
		}
	}
	@Override
	public void actionPerformed(ActionEvent e){
		JButton button = (JButton)e.getSource();
		if(super.soundPlayer.isPlaying()) {
	    	button.setText("Play");
	    	super.soundPlayer.pause();
	    }else {
	    	button.setText("Stop");
	    	try {
				super.soundPlayer.resume();
			} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    }
		
	}
}
