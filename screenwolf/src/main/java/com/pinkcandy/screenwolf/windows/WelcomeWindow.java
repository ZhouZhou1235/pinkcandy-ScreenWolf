package com.pinkcandy.screenwolf.windows;

import java.util.ArrayList;
import java.awt.*;
import java.net.URL;
import java.net.URLClassLoader;
import javax.swing.*;
import com.pinkcandy.screenwolf.Launcher;
import com.pinkcandy.screenwolf.base.PetBase;
import com.pinkcandy.screenwolf.base.WindowBase;
import com.pinkcandy.screenwolf.utils.GUtil;
import com.pinkcandy.screenwolf.utils.ResourceReader;
import com.pinkcandy.screenwolf.bean.GameInfoData;
import com.pinkcandy.screenwolf.bean.PetData;
import com.pinkcandy.screenwolf.utils.GsonUtil;
import com.pinkcandy.screenwolf.utils.JarFileUtil;


/**
 * 开始窗口
 * 启动器完成加载后显示的窗口，能选择桌宠和控制游戏的状态。
 */
public class WelcomeWindow extends WindowBase {
    private ArrayList<JButton> petButtonsList = new ArrayList<>();
    private JPanel welcomePanel = new JPanel();
    private JButton playButton,clearButton,reloadButton,exitButton,infoButton,addPetButton;
    private Launcher launcher;
    private JPanel petsContentPanel;
    public WelcomeWindow(Launcher launcher){
        super("ScreenWolf",GUtil.DEFAULT_windowSize);
        this.launcher = launcher;
        initWelcomeWindow();
        this.setVisible(true);
    }
    // 初始化窗口
    private void initWelcomeWindow(){
        setupWindowProperties();
        setupWelcomePanel();
        addTitleAndButtonPanel();
        loadPetsFromJars();
        this.add(welcomePanel);
        this.updateWindow();
    }
    // 设置窗口属性
    private void setupWindowProperties(){
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setIconImage(ResourceReader.getResourceAsImageIcon("images/icon.png").getImage());
        this.setSize(
            (int)(GUtil.SCREEN_dimension.width/1.5),
            (int)(GUtil.SCREEN_dimension.height/1.5)
        );
        GUtil.setWindowCenter(this);
    }
    // 设置欢迎面板
    private void setupWelcomePanel(){
        this.welcomePanel = new JPanel(new BorderLayout(5,5));
        this.welcomePanel.setBackground(new Color(245,245,250));
        this.welcomePanel.setBorder(BorderFactory.createEmptyBorder(
            GUtil.DEFAULT_textSize/2, 
            GUtil.DEFAULT_textSize/2,
            GUtil.DEFAULT_textSize/2,
            GUtil.DEFAULT_textSize/2
        ));
    }
    // 添加标题和按钮面板
    private void addTitleAndButtonPanel(){
        JPanel northPanel = new JPanel(new BorderLayout());
        ImageIcon logo = ResourceReader.getResourceAsImageIcon("images/logo.png");
        int logoWidth = (int)(GUtil.DEFAULT_windowSize.width*0.25);
        logo = GUtil.scaleImageIcon(logo, logoWidth);
        JLabel titleLabel = new JLabel(logo,SwingConstants.CENTER);
        northPanel.add(titleLabel, BorderLayout.NORTH);
        JPanel buttonPanel = createButtonPanel();
        northPanel.add(buttonPanel, BorderLayout.SOUTH);
        welcomePanel.add(northPanel, BorderLayout.NORTH);
        JLabel versionLabel = createVersionLabel();
        welcomePanel.add(versionLabel, BorderLayout.SOUTH);
    }
    // 创建按钮面板
    private JPanel createButtonPanel(){
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,15,5));
        int buttonSize = GUtil.DEFAULT_textSize * 2;
        initializeButtons(buttonSize);
        setupButtonActions();
        addButtonsToPanel(buttonPanel);
        return buttonPanel;
    }
    // 初始化按钮
    private void initializeButtons(int buttonSize){
        playButton = GUtil.createIconButton("images/button_play.png","开始游戏 play",buttonSize);
        clearButton = GUtil.createIconButton("images/button_stop.png","结束游戏 stop",buttonSize);
        reloadButton = GUtil.createIconButton("images/button_reload.png","重新加载 reload",buttonSize);
        exitButton = GUtil.createIconButton("images/button_exit.png","退出程序 exit",buttonSize);
        infoButton = GUtil.createIconButton("images/button_info.png","游戏介绍 info",buttonSize);
        addPetButton = GUtil.createIconButton("images/button_add_pet.png","添加宠物 add pet",buttonSize);
        clearButton.setEnabled(false);
    }
    // 设置按钮动作
    private void setupButtonActions(){
        playButton.addActionListener(e->handlePlayButton());
        clearButton.addActionListener(e->launcher.stopGame());
        reloadButton.addActionListener(e->launcher.reloadLauncher());
        exitButton.addActionListener(e->System.exit(0));
        infoButton.addActionListener(e->launcher.getInfoWindow().setVisible(true));
        addPetButton.addActionListener(e->handleAddPetButton());
    }
    // 处理播放按钮点击
    private void handlePlayButton(){
        if(!launcher.getPetListCopy().isEmpty()){launcher.playGame();}
        else{JOptionPane.showMessageDialog(this,"没有选择宠物 no pet have selected");}
    }
    // 处理添加宠物按钮点击
    private void handleAddPetButton(){
        int copiedCount = GUtil.copyFilesWithDialog(
            this, 
            "选择宠物JAR文件 select pet jar", 
            "JAR文件", 
            "jar",
            GUtil.GAME_petsPath
        );
        if(copiedCount>0){launcher.reloadLauncher();}
    }
    // 添加按钮到面板
    private void addButtonsToPanel(JPanel buttonPanel){
        buttonPanel.add(playButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(reloadButton);
        buttonPanel.add(exitButton);
        buttonPanel.add(infoButton);
        buttonPanel.add(addPetButton);
    }
    // 创建版本标签
    private JLabel createVersionLabel(){
        GameInfoData gameInfoData = GsonUtil.json2Bean(ResourceReader.readResourceAsString("screenwolf.json"),GameInfoData.class);
        JLabel versionLabel = new JLabel("版本 version:"+gameInfoData.getVersion()+" 作者 owner:"+gameInfoData.getOwner(),SwingConstants.CENTER);
        versionLabel.setFont(GUtil.DEFAULT_font.deriveFont(Font.PLAIN, (int)(GUtil.DEFAULT_textSize * 0.8)));
        versionLabel.setForeground(new Color(120, 120, 120));
        versionLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        return versionLabel;
    }
    // 从JAR文件加载宠物
    private void loadPetsFromJars(){
        JScrollPane scrollPane = createScrollPane();
        petsContentPanel = createPetsContentPanel();
        refreshPetsContent();
        scrollPane.setViewportView(petsContentPanel);
        welcomePanel.add(scrollPane, BorderLayout.CENTER);
    }
    // 创建滚动面板
    private JScrollPane createScrollPane(){
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(30);
        scrollPane.getViewport().addMouseWheelListener(e->{
            JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
            if(verticalScrollBar!=null && verticalScrollBar.isVisible()){
                int rotation = e.getWheelRotation();
                int unitIncrement = verticalScrollBar.getUnitIncrement();
                int newValue = verticalScrollBar.getValue() + (rotation * unitIncrement * 3);
                newValue = Math.max(
                    verticalScrollBar.getMinimum(), 
                    Math.min(newValue,verticalScrollBar.getMaximum()-verticalScrollBar.getVisibleAmount())
                );
                verticalScrollBar.setValue(newValue);
                e.consume();
            }
        });
        return scrollPane;
    }
    // 创建宠物内容面板
    private JPanel createPetsContentPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        return panel;
    }
    // 刷新宠物内容面板
    public void refreshPetsContent(){
        petsContentPanel.removeAll();
        petButtonsList.clear();
        String[] petJars = GUtil.scanDir(GUtil.GAME_petsPath,".jar");
        if (petJars.length == 0){
            addNoPetsLabel();
        } else {
            addPetEntries(petJars);
        }
        petsContentPanel.add(Box.createVerticalGlue());
        petsContentPanel.revalidate();
        petsContentPanel.repaint();
    }
    // 添加无宠物标签
    private void addNoPetsLabel(){
        JLabel noPetsLabel = new JLabel("没有宠物，点击按钮添加。 No pets, click the button to add.", SwingConstants.CENTER);
        noPetsLabel.setFont(GUtil.DEFAULT_font.deriveFont(Font.ITALIC, GUtil.DEFAULT_textSize));
        noPetsLabel.setForeground(new Color(150, 150, 150));
        noPetsLabel.setBorder(BorderFactory.createEmptyBorder(50, 10, 50, 10));
        petsContentPanel.add(noPetsLabel);
    }
    // 添加宠物条目
    private void addPetEntries(String[] petJars){
        for(String jarName:petJars){
            try{
                String jarPath = GUtil.GAME_petsPath+jarName;
                PetData petData = loadPetDataFromJar(jarPath);
                if(petData!=null){
                    JPanel petEntryPanel = createPetEntryPanel(jarPath,jarName,petData);
                    petsContentPanel.add(petEntryPanel);
                    petsContentPanel.add(Box.createRigidArea(new Dimension(0, GUtil.DEFAULT_textSize)));
                }
            }catch(Exception e){e.printStackTrace();}
        }
    }
    // 创建宠物的面板
    private JPanel createPetEntryPanel(String jarPath,String jarName,PetData petData){
        JPanel petEntryPanel = new JPanel(new BorderLayout(10,5));
        setupPetEntryPanelStyle(petEntryPanel);
        JPanel leftPanel = createPetIconPanel(jarPath);
        JPanel rightPanel = createPetInfoPanel(jarPath, petData);
        int minHeight = GUtil.DEFAULT_textSize * 8;
        petEntryPanel.setMinimumSize(new Dimension(0, minHeight));
        petEntryPanel.setPreferredSize(new Dimension(0, minHeight + GUtil.DEFAULT_textSize));
        petEntryPanel.add(leftPanel,BorderLayout.WEST);
        petEntryPanel.add(rightPanel,BorderLayout.CENTER);
        return petEntryPanel;
    }
    // 设置宠物条目面板样式
    private void setupPetEntryPanelStyle(JPanel panel){
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,220,220),3),
            BorderFactory.createEmptyBorder(3,3,3,3)
        ));
        panel.setBackground(Color.WHITE);
    }
    // 创建宠物图标面板
    private JPanel createPetIconPanel(String jarPath){
        JPanel leftPanel = new JPanel(new BorderLayout());
        JLabel iconLabel = createPetIconLabel(jarPath);
        leftPanel.add(iconLabel,BorderLayout.CENTER);
        leftPanel.setPreferredSize(new Dimension(GUtil.DEFAULT_textSize*4,GUtil.DEFAULT_textSize*4));
        return leftPanel;
    }
    // 创建宠物图标标签
    private JLabel createPetIconLabel(String jarPath){
        JLabel iconLabel = new JLabel();
        try{
            ImageIcon icon = new ImageIcon(JarFileUtil.readByteInJarFile(jarPath,"assets/icon.png"));
            if(icon!=null){
                icon = GUtil.scaleImageIcon(icon,GUtil.DEFAULT_textSize*4);
                iconLabel.setIcon(icon);
                iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            }
        }catch(Exception e){
            ImageIcon defaultIcon = ResourceReader.getResourceAsImageIcon("images/icon.png");
            defaultIcon = GUtil.scaleImageIcon(defaultIcon, GUtil.DEFAULT_textSize*4);
            iconLabel.setIcon(defaultIcon);
        }
        return iconLabel;
    }
    // 创建宠物信息面板
    private JPanel createPetInfoPanel(String jarPath, PetData petData){
        JPanel rightPanel = new JPanel(new BorderLayout(5,5));
        JPanel topPanel = createPetTopPanel(jarPath, petData);
        JScrollPane descScroll = createPetDescriptionScroll(petData);
        rightPanel.add(topPanel, BorderLayout.NORTH);
        rightPanel.add(descScroll, BorderLayout.CENTER);
        int minWidth = GUtil.DEFAULT_textSize * 10;
        int preferredWidth = GUtil.DEFAULT_textSize * 20;
        rightPanel.setMinimumSize(new Dimension(minWidth, 0));
        rightPanel.setPreferredSize(new Dimension(preferredWidth, 0));
        return rightPanel;
    }
    // 创建宠物顶部面板
    private JPanel createPetTopPanel(String jarPath, PetData petData){
        JPanel topPanel = new JPanel(new BorderLayout(10,0));
        JLabel nameLabel = createPetNameLabel(petData);
        JButton selectButton = createSelectButton(jarPath, petData);
        topPanel.add(nameLabel,BorderLayout.CENTER);
        topPanel.add(selectButton,BorderLayout.EAST);
        return topPanel;
    }
    // 创建宠物名称标签
    private JLabel createPetNameLabel(PetData petData){
        JLabel nameLabel = new JLabel(petData.getName());
        nameLabel.setFont(GUtil.DEFAULT_font.deriveFont(Font.BOLD, (int)(GUtil.DEFAULT_textSize*1.2)));
        nameLabel.setForeground(new Color(70,70,70));
        return nameLabel;
    }
    // 创建选择按钮
    private JButton createSelectButton(String jarPath, PetData petData){
        JButton selectButton = GUtil.createIconButton("images/button_import.png","选择 select",GUtil.DEFAULT_textSize*2);
        selectButton.addActionListener(e->handleSelectPet(jarPath, petData, selectButton));
        petButtonsList.add(selectButton);
        return selectButton;
    }
    // 处理选择宠物
    private void handleSelectPet(String jarPath, PetData petData, JButton selectButton){
        try{
            PetBase pet = loadPetFromJar(jarPath,petData.getMainClass(),launcher);
            if(pet!=null){
                launcher.addPetToLauncher(pet);
                selectButton.setEnabled(false);
                selectButton.setToolTipText("已选择 selected");
            }
        }catch(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载宠物失败 load pet failed: " + ex.getMessage(), "错误 error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // 创建宠物描述滚动面板
    private JScrollPane createPetDescriptionScroll(PetData petData){
        JTextArea descArea = createDescriptionTextArea(petData);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createEmptyBorder());
        descScroll.setOpaque(false);
        descScroll.getViewport().setOpaque(false);
        descScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        descScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        int maxHeight = GUtil.DEFAULT_textSize * 8;
        int preferredHeight = Math.min(maxHeight, descArea.getPreferredSize().height);
        descScroll.setPreferredSize(new Dimension(0, preferredHeight));
        return descScroll;
    }
    // 创建描述文本区域
    private JTextArea createDescriptionTextArea(PetData petData){
        JTextArea descArea = new JTextArea("["+petData.getId()+"]"+petData.getInfo());
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setOpaque(false);
        descArea.setFont(GUtil.DEFAULT_font.deriveFont(Font.PLAIN, GUtil.DEFAULT_textSize));
        descArea.setForeground(new Color(100, 100, 100));
        descArea.setHighlighter(null);
        descArea.setFocusable(false);
        descArea.setMargin(new Insets(5,0,5,0));
        int lineCount = Math.max(1, descArea.getLineCount());
        int lineHeight = descArea.getFontMetrics(descArea.getFont()).getHeight();
        int preferredHeight = lineCount * lineHeight + 10;
        descArea.setPreferredSize(new Dimension(0, preferredHeight));
        descArea.setMinimumSize(new Dimension(0, lineHeight + 10));
        return descArea;
    }
    // 从JAR中加载宠物数据
    private PetData loadPetDataFromJar(String jarPath) throws Exception{
        String json = new String(JarFileUtil.readByteInJarFile(
            jarPath,
            "pet_data.json"
        ));
        return GsonUtil.json2Bean(json,PetData.class);
    }
    // 从JAR加载宠物实现类
    private PetBase loadPetFromJar(String jarPath, String className, Launcher launcher) throws Exception {
        @SuppressWarnings("deprecation")
        URL jarUrl = new URL("file:" + jarPath);
        URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, getClass().getClassLoader());
        try{
            Class<?> petClass = classLoader.loadClass(className);
            PetBase pet = (PetBase)petClass.getConstructor(Launcher.class).newInstance(launcher);
            pet.setClassLoader(classLoader);
            return pet;
        }catch(Exception e){classLoader.close();throw e;}
    }
    // 更新窗口为开始游戏状态
    public void updateWindowToPlayState(){
        for(JButton petButton:petButtonsList){petButton.setEnabled(false);}
        playButton.setEnabled(false);
        clearButton.setEnabled(true);
        exitButton.setEnabled(false);
        reloadButton.setEnabled(false);
        addPetButton.setEnabled(false);
    }
    // 更新窗口为结束游戏状态
    public void updateWindowToStopState(){
        for(JButton petButton:petButtonsList){petButton.setEnabled(true);}
        playButton.setEnabled(true);
        clearButton.setEnabled(false);
        exitButton.setEnabled(true);
        reloadButton.setEnabled(true);
        addPetButton.setEnabled(true);
    }
    // 重新加载宠物列表
    public void reloadPets(){
        refreshPetsContent();
    }
}
