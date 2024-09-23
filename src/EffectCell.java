import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.sound.sampled.LineEvent;
import javax.swing.BorderFactory;

public class EffectCell extends SoundCell {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6480787076536786911L;

	public EffectCell() {
		super("");
	}
	public EffectCell(String name) {
		super(name);
	}
	public EffectCell(File file) {
		super(file);
	}
	@Override
	public void update(LineEvent event) {
		//System.out.println(event.getType());
    	//System.out.println(soundPlayer.getCurrentFrame());
    	//System.out.println(soundPlayer.getFrameLength());
		if(event.getType().equals(LineEvent.Type.STOP)) {
	    	
	    	if(super.soundPlayer.getCurrentFrame()==soundPlayer.getFrameLength()) {
		    	timeUpdater.stop();
	    		soundPlayer.setCurrentFrame(0);
	    		updateTimeSlider();
	    	}
		}
	}
	@Override
	protected void setupSize() {
		this.setPreferredSize(new Dimension(SoundCell.WIDTH,SoundCell.HEIGHT));
		this.setMinimumSize(getPreferredSize());
		this.setMaximumSize(getPreferredSize());
		this.setBorder(BorderFactory.createLineBorder(Color.black));
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		super.soundPlayer.setCurrentFrame(0);

    	//System.out.println("Timer Running?: "+timeUpdater.isRunning());
		if(!(super.timeUpdater.isRunning())) {
			super.timeUpdater.start();
		}
    	//System.out.println("Timer Running Now?: "+timeUpdater.isRunning());
		super.updateTimeSlider();
    	super.soundPlayer.play();
	}
	
}
