import java.awt.Dimension;

public class HiddenCell extends SoundCell {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2061720960221667411L;
	public HiddenCell() {
		super("");
	}
	@Override
	protected void setupSize() {
		this.setPreferredSize(new Dimension(SoundCell.WIDTH,SoundCell.HEIGHT));
		this.setMinimumSize(getPreferredSize());
		this.setMaximumSize(getPreferredSize());
	}
}
