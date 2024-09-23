import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

public class AudioManager extends JFrame implements DragGestureListener{

	private static final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' };
	private static final long serialVersionUID = -360207170452615858L;
	public static AudioManager currentManager=null;
	private static SoundCell currentlyDraggedCell;
	
	
	private JProgressBar loadingBar;
	private int progress=0;
	private JLabel loadingText;
	
	
    private JPanel songPanel;
    private int songPanelSize = 3;
    
    private JPanel effectPanel;
    private int effectPanelSize = 6;
    
    private int storageWidth = 6;
    private int storageHeight =5;
    
    private JPanel effectStoragePanel;
    private JPanel songStoragePanel;
    private ArrayList<JScrollPane> scrollStorage = new ArrayList<JScrollPane>();
    
    private ArrayList<SoundCell> loadedSoundCells = new ArrayList<SoundCell>();
    
    
    private MyDropTargetListener[] songTargets = new MyDropTargetListener[songPanelSize];
    private MyDropTargetListener[] effectTargets = new MyDropTargetListener[effectPanelSize];
    
    private MyDropTargetListener effectStorageTarget;
    private MyDropTargetListener songStorageTarget;
    private ArrayList<MyDropTargetListener> storageListeners = new ArrayList<MyDropTargetListener>();
    
    private ArrayList<String> tabNames = new ArrayList<String>();
    
    private ImageIcon desktopIcon =new ImageIcon("Img\\AudioBrady.png");
    
    private int loadingIconSize =200;
    private ImageIcon loadingScreenIcon = new ImageIcon(desktopIcon.getImage().getScaledInstance(loadingIconSize, loadingIconSize,Image.SCALE_DEFAULT));

    private JMenuBar menuBar;
    
    public AudioManager() {
    	AudioManager.currentManager=this;
        this.setTitle("Brady C. Emmelhainz's Cool Audio Manager Thingy");
        this.setIconImage(desktopIcon.getImage());
        
        initLoadingScreen();
        initMainUI();
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    private void initMainUI() {
    	initMenuBar();
    	initSongPanel();
    	initEffectPanel();
    	initStoragePanel();
    	
    	//createLayout(songPanel,effectPanel,scrollStorage);
    	
    	
    }
    private void initLoadingScreen() {
        //this.setVisible(true);

        Container pane = getContentPane();
        clearContainer(pane);
        BoxLayout gl = new BoxLayout(pane,BoxLayout.PAGE_AXIS);
        pane.setLayout(gl);
        
        JPanel titlePanel = new JPanel();
        JLabel title = new JLabel("Brady Emmelhainz's High Quality Audio Management Software");
        titlePanel.add(title);
        
        JPanel loadingBarPanel = new JPanel();
    	loadingBar = new JProgressBar(0,calculateMaximumLoadTime());
    	loadingBar.setValue(0);
    	loadingBar.setPreferredSize(new Dimension(400, 15));
    	loadingBar.setStringPainted(true);
    	loadingBarPanel.add(loadingBar);

        JPanel loadingTextPanel = new JPanel();
    	loadingText = new JLabel("Loading Stuff");
    	loadingTextPanel.add(loadingText);
    	loadingTextPanel.setPreferredSize(new Dimension(500,50));
    	
    	JPanel imagePanel = new JPanel();
    	JLabel image = new JLabel(loadingScreenIcon);
    	imagePanel.add(image);
    	
    	pane.add(titlePanel);
    	pane.add(imagePanel);
    	pane.add(loadingBarPanel);
    	pane.add(loadingTextPanel);
    	
        pack();
        this.setVisible(true);
        
    }
    private void updateLoadingBar(String update) {
    	updateLoadingBar(update,1);
    	
    }
    private void updateLoadingBar(String update,int size) {
    	
    	SwingUtilities.invokeLater(new Runnable(){
    		public void run() {
	    		progress+=size;
	        	loadingBar.setValue(progress);
	        	loadingText.setText(update);
    		}
        });
    }
    /**This method will initialize the menu bar and all its menus and menu items
     * 
     */
    private void initMenuBar() {
    	menuBar = new JMenuBar();
    	//File Menu Stuff
    	JMenu fileMenu = new JMenu("File");
    	JMenuItem fileMenuInfo1 = new JMenuItem("Here is where you can do file related actions.");
    	JMenuItem fileMenuInfo2 = new JMenuItem("As long as you are not trying to replace a file that already exists. ");
    	JMenuItem fileMenuInfo3 = new JMenuItem("Because I haven't got the program to be able to do that safely (the file is technically in use by the program)");
    	JMenuItem fileMenuInfo4 = new JMenuItem("Also only .mp3 files and .wav files are supported");
    	//New as in making new folders and files
    	JMenu newMenu = new JMenu("New");
    	JMenuItem addAudioFolder = new JMenuItem("Folder");
    	addAudioFolder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                	JTextField input = new JTextField();
                	String folderName=JOptionPane.showInputDialog(input, "Folder Name (must be valid)");
                	String filename = "Audio\\"+folderName;
                	File dir = new File(filename);
                	boolean valid = true;
                	try {
                		Paths.get(filename);
            		}catch(InvalidPathException e){
                		valid=false;
                	}
                	if(dir.isFile()) {
                		JOptionPane.showMessageDialog(currentManager,"Folder Already Exists. Dummy", "Folder Already Exists",JOptionPane.ERROR_MESSAGE);
                	}else if(valid) {
                		JOptionPane.showMessageDialog(currentManager,"Your Folder Name Is Invalid", "Big Error",JOptionPane.ERROR_MESSAGE);
                	}else {
                		dir.mkdir();
                	}
            	}
            });
    	
    	JMenuItem addAudioFile = new JMenuItem("File");
    	

    	addAudioFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
            	JFileChooser fc = new JFileChooser();
            	fc.setFileFilter(new AudioFileFilter());
            	fc.setAcceptAllFileFilterUsed(false);
            	//In response to a button click:
            	int returnVal = fc.showOpenDialog(currentManager);
            	File audioFile = fc.getSelectedFile();
            	System.out.println(audioFile.getName());
            	Object[] options = tabNames.toArray();
            	Object selectedObject =JOptionPane.showInputDialog(currentManager, "Choose", "Menu", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            	String selectedString = selectedObject.toString();
            		
            	File newLocation = new File("Audio\\"+selectedString+"\\"+audioFile.getName());
            	try {
					Files.copy(audioFile.toPath(), newLocation.toPath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					int response = JOptionPane.showConfirmDialog(currentManager,"Do you want to replace this file", "File Already Exists",JOptionPane.YES_NO_OPTION);
					
					if(response==JOptionPane.YES_OPTION) {
						for(int i=0;i<loadedSoundCells.size();i++) {
							SoundCell cell =loadedSoundCells.get(i);
							if(cell.soundName.equals(audioFile.getName())) {
								cell.close();
								break;
							}
						}
						try {
							//TODO Needs to free up the currently used song from storage before trying to replace it. 
							//TODO Need to have way of easily finding the SoundCell, which is hard considering how I implemented the swing worker for initStorage()
							
							Files.copy(audioFile.toPath(), newLocation.toPath(),StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							
							JOptionPane.showMessageDialog(currentManager,"Well Something went Wrong", "Big Error",JOptionPane.ERROR_MESSAGE);
						}
						
					}
				}
            	}
            
           
            });
    	
    	newMenu.add(addAudioFile);
    	newMenu.add(addAudioFolder);

    	fileMenu.add(newMenu);
    	fileMenu.add(fileMenuInfo1);
    	fileMenu.add(fileMenuInfo2);
    	fileMenu.add(fileMenuInfo3);
    	fileMenu.add(fileMenuInfo4);
    	
    	
    	//Help Menu Stuff
    	JMenu helpMenu = new JMenu("Help");
    	JMenuItem helpMenuInfo = new JMenuItem("TIP: Adding sounds via the Menu is very iffy. Its easier to do things by accessing the Audio Folder directly");
    	helpMenu.add(helpMenuInfo);
    	
    	JMenu soonMenu = new JMenu("Upcoming Features");

    	JMenuItem feature0 = new JMenuItem("Some Features I may or may not get around to adding eventually");
    	JMenuItem feature1 = new JMenuItem("Keybinding Functionality");
    	JMenuItem feature2 = new JMenuItem("A Flexable GUI that doesn't have as many fixed size values and the ability to alter the number of song and effect cells");
    	JMenuItem feature3 = new JMenuItem("A better menu system");
    	JMenuItem feature4 = new JMenuItem("Microphone support for some reason (unlikely)");
    	JMenuItem feature5 = new JMenuItem("Mac Support or something. I don't own one so this would be difficult");

    	soonMenu.add(feature0);
    	soonMenu.add(feature1);
    	soonMenu.add(feature2);
    	soonMenu.add(feature3);
    	soonMenu.add(feature4);
    	soonMenu.add(feature5);

    	menuBar.add(fileMenu);
    	menuBar.add(helpMenu);
    	menuBar.add(soonMenu);
    	
    }
    
    
    
    /**Initializes the ui and the drag and drop code for Song Panel of the Manager.
     * This Panel is capable of looping the sound effect played in it, not just 
     * 
     */
    private void initSongPanel() {
    	songPanel = new JPanel();
    	songPanel.setLayout(new BoxLayout(songPanel,BoxLayout.X_AXIS));
    	for(int i=0;i<songPanelSize;i++) {
        	JPanel soundCell = new SongCell("");
        	songPanel.add(soundCell);
        	songTargets[i] = new MyDropTargetListener(soundCell);
        }
    	songPanel.add(Box.createHorizontalGlue());
    	//songPanel.add(new Box.Filler(getMinimumSize(), new Dimension(10,SoundCell.HEIGHT), getMaximumSize()));

    	
    	//songPanel.setBackground(Color.red);
    }
    
    private void initEffectPanel() {
		effectPanel = new JPanel();
    	
    	//effectPanel.setLayout(new GridLayout(1,4));
    	effectPanel.setLayout(new BoxLayout(effectPanel,BoxLayout.X_AXIS));
    	for(int i=0;i<effectPanelSize;i++) {
        	JPanel soundCell = new EffectCell("");
        	effectPanel.add(soundCell);
        	effectTargets[i] = new MyDropTargetListener(soundCell);
        }
    	effectPanel.add(Box.createHorizontalGlue());
    	
    	//effectPanel.setBackground(Color.GREEN);
    }
    
    
    private void initStoragePanel() {
    	File audioDir = new File("Audio");
    	File[] audioFolders = audioDir.listFiles();
    	for(int i=0;i<audioFolders.length;i++) {
        	System.out.println(audioFolders[i].getName());
    		tabNames.add(audioFolders[i].getName());
        	
    	}
    	SwingWorker<ArrayList<ArrayList<SoundCell>>, Void> worker = new SwingWorker<ArrayList<ArrayList<SoundCell>>, Void>() {
    	    @Override
    	    public ArrayList<ArrayList<SoundCell>> doInBackground() {
    	    	ArrayList<JScrollPane> storageForScrolls = new ArrayList<JScrollPane>();
    	        ArrayList<ArrayList<SoundCell>> tabStorage = new ArrayList<ArrayList<SoundCell>>();
    	        
    	    	//TODO change the worker to use nested arraylists of sound cells rather than ScrollPanes
    	    	for(int i=0;i<audioFolders.length;i++) {
    	    		
    	        	ArrayList<SoundCell> soundCellTab = new ArrayList<SoundCell>();
    	        	tabStorage.add(soundCellTab);
    	        	
    	        	File dir = audioFolders[i];
    	        	File[] files = dir.listFiles();
    	        	
    	        	if (files != null) {
    	        		int j;
    	        	    for (j=0; j<files.length;j++) {
    	        	    	updateLoadingBar("Loading: "+files[j].getName());

    	    	        	//JPanel soundCell = new SoundCell(files[j].getAbsolutePath());
    	        	    	SoundCell cell =new SoundCell(files[j]);
    	    	        	soundCellTab.add(cell);
    	    	        	/*
    	    	        	c.gridx = j%storageWidth;
    	    	        	c.gridy = j/storageWidth;
    	    	        	storagePanel.add(soundCell,c);
    	    	        	*/
    	    	        	

    	        	    }
    	        	    
    	        	    updateLoadingBar("Loading Decoy Sound Cells");
    	        	    for(j=j;j<(storageWidth*storageHeight);j++) {
    	    	        	SoundCell fakeCell = new HiddenCell();
    	    	        	//fakeCell.setPreferredSize(new Dimension(SoundCell.WIDTH,SoundCell.HEIGHT));
    	        	    	/*
    	                	c.gridx = j%storageWidth;
    	                	c.gridy = j/storageWidth;
    	        	    	storagePanel.add(fakeCell,c);
    	                	*/
    	    	        	soundCellTab.add(fakeCell);
    	        	    }
    	        	    
    	            }
    	    		
    	    	}
    	    	
    	    	
    	    	
    	        return tabStorage;
    	    }
    	    //TODO add the soundcells from nested arraylists to scrollPanels
    	    @Override
    	    public void done() {
    	        try {
					//scrollStorage = get();
    	        	ArrayList<ArrayList<SoundCell>> tabStorage = get();
					
					for(int i=0;i<tabStorage.size();i++) {
						ArrayList<SoundCell> cells = tabStorage.get(i);
						JPanel storagePanel = new JPanel();
	    	    		JScrollPane storageScroll= new JScrollPane(storagePanel);
	    	    		scrollStorage.add(storageScroll);
	    	    		
	    	        	storageScroll.setPreferredSize(new Dimension(SoundCell.WIDTH*4,SoundCell.HEIGHT*5));
	    	        	storagePanel.setLayout(new GridBagLayout());
	    	        	GridBagConstraints c = new GridBagConstraints();
	    	        	
	    	        	//c.fill = GridBagConstraints.BOTH;
	    	        	c.insets = new Insets(0,0,0,0);
	    	        	c.weightx = 1;
	    	        	c.weighty = 1;
	    	        	c.ipadx = 0;
	    	        	c.ipady = 0;
	    	        	c.anchor = 	GridBagConstraints.FIRST_LINE_START;
	    	        	
	    	        	

    	        		int j;
    	        	    for (j=0; j<cells.size();j++) {
    	        	    	
    	    	        	c.gridx = j%storageWidth;
    	    	        	c.gridy = j/storageWidth;
    	    	        	SoundCell cell =cells.get(j);
    	    	        	storagePanel.add(cell,c);
    	    	        	if(!cell.soundName.equals("")) {
    	    	        		loadedSoundCells.add(cell);
    	    	        	}
    	    	        	
    	        	    }
       	        	 	storageListeners.add(new MyDropTargetListener(storagePanel));
					}
					

					setVisible(false);
			    	createLayout();
					setVisible(true);
					
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	    }
    	};
    	worker.execute();
    	
    }
    private int calculateMaximumLoadTime() {
    	int load=0;
    	File audioDir = new File("Audio");
    	File[] audioFolders = audioDir.listFiles();
    	for(int i=0;i<audioFolders.length;i++) {
        	load+=audioFolders[i].listFiles().length;
        	
    	}
    	//Fake Cells
    	load++;
    	return load;
    }
    
    public void dragGestureRecognized(DragGestureEvent event) {

        var cursor = Cursor.getDefaultCursor();
        var panel = (SoundCell) event.getComponent();

        var file = panel.getSoundFile();
        //System.out.println(file);

        if (event.getDragAction() == DnDConstants.ACTION_COPY) {
            cursor = DragSource.DefaultCopyDrop;
        }
        AudioManager.setCurrentlyDraggedCell(panel);

        event.startDrag(cursor, new TransferableFile(file));
    }

    private class MyDropTargetListener extends DropTargetAdapter {

    	
        @SuppressWarnings("unused")
		private final DropTarget dropTarget;
        private final JPanel panel;
        private String targetType;
        
        
        public MyDropTargetListener(JPanel targetPanel) {
            this.panel = targetPanel;
            targetType = targetPanel.getClass().getName();
            
            dropTarget = new DropTarget(targetPanel, DnDConstants.ACTION_COPY,this, true, null);
        }


        public void drop(DropTargetDropEvent event) {

            try {
            	//System.out.println("Dragged");
                var tr = event.getTransferable();
                var file = (File) tr.getTransferData(TransferableFile.fileFlavor);
                //System.out.println("And...");
                
                //System.out.println(event.isDataFlavorSupported(TransferableFile.fileFlavor));
                if (event.isDataFlavorSupported(TransferableFile.fileFlavor)) {
                	SoundCell draggedCell = AudioManager.getCurrentlyDraggedCell();
                	try{
                		SongCell oldCell = ((SongCell)draggedCell);
                		oldCell.reset();
                		//System.out.println("Test 1");
                	}catch(Exception e) {
                		//System.out.println("Next");
                		
                		try {
                    		//System.out.println(draggedCell.getClass().getName());
                			EffectCell oldCell = ((EffectCell)draggedCell);
                            oldCell.reset();
                    		//System.out.println("Test 2");
                		}catch(Exception g) {
                    		//System.out.println("Test 3");
                		}
                	}

                    event.acceptDrop(DnDConstants.ACTION_COPY);
                    //System.out.println("Dropped");
                    //This is where the magic of conversion happens

                    //System.out.println(targetType);
                    
                    boolean songTest = targetType.equals(SongCell.class.getName());
                    boolean effectTest = targetType.equals(EffectCell.class.getName());
                    boolean soundTest = targetType.equals(SoundCell.class.getName());
                    //System.out.println(file);
                    if(soundTest) {
                    	SoundCell cell = (SoundCell) this.panel;
                        cell.newSound(file);
                        cell.checkVisibility();
                        //cell.cellStatusCheck();
                    }else if(effectTest){
                        //System.out.println("This should only happen once");
                    	EffectCell cell = (EffectCell) this.panel;
                        cell.newSound(file);
                        cell.checkVisibility();
                    }else if(songTest) {
                    	SongCell cell = (SongCell) this.panel;
                        cell.newSound(file);
                        cell.checkVisibility();
                    }else {
                    	
                    }
                    event.dropComplete(true);
                    return;
                }

                event.rejectDrop();
            } catch (Exception e) {
            	System.out.println("huh");
                e.printStackTrace();
                event.rejectDrop();
            }
        }
        
    }
    
    private void clearContainer(Container pane) {
    	pane.removeAll();
    	pane.revalidate();
    	pane.repaint();
    }
    private void createLayout() {
    	/*
    	for(int i=0;i<loadedSoundCells.size();i++) {
			SoundCell cell =loadedSoundCells.get(i);
			if(cell.soundName.equals("Bog-Creatures-On-the-Move.mp3")) {
				cell.close();
				loadedSoundCells.remove(i);
				break;
			}
		}
		*/
        Container pane = getContentPane();
        clearContainer(pane);
        BoxLayout gl = new BoxLayout(pane,BoxLayout.PAGE_AXIS);
        pane.setLayout(gl);
        this.setJMenuBar(menuBar);

    	JTabbedPane songPane = new JTabbedPane();
    	songPane.addTab("Songs", songPanel);
    	pane.add(songPane);
    	
    	JTabbedPane effectPane = new JTabbedPane();
    	effectPane.addTab("Effects", effectPanel);
    	pane.add(effectPane);
        
    	JTabbedPane tabbedPane = new JTabbedPane();
    	for(int i=0;i<scrollStorage.size();i++) {
    		tabbedPane.addTab(tabNames.get(i), scrollStorage.get(i));
        	pane.add(tabbedPane);
    	}
        this.setPreferredSize(new Dimension(getPreferredSize().width+40,getPreferredSize().height+37));

        pack();
	    System.out.println(scrollStorage.get(0).getSize());
	    System.out.println(scrollStorage.get(0).getSize().height-SoundCell.HEIGHT*storageHeight);
	    System.out.println(scrollStorage.get(0).getPreferredSize());
	    System.out.println(scrollStorage.get(0).getPreferredSize().height-SoundCell.HEIGHT*storageHeight);
    }
    public static SoundCell getCurrentlyDraggedCell() {
    	return AudioManager.currentlyDraggedCell;
    }
    public static void setCurrentlyDraggedCell(SoundCell source) {
    	AudioManager.currentlyDraggedCell = source;
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {

            var ex = new AudioManager();
            ex.setVisible(true);
        });
    }
    
    
}
