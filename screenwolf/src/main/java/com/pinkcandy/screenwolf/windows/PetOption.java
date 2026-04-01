package com.pinkcandy.screenwolf.windows;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;
import com.pinkcandy.screenwolf.base.PetBase;
import com.pinkcandy.screenwolf.base.WindowBase;
import com.pinkcandy.screenwolf.utils.GUtil;
import com.pinkcandy.screenwolf.utils.JarFileUtil;
import com.pinkcandy.screenwolf.utils.ResourceReader;


/**
 * 宠物选项面板
 * 每只宠物都有一个专属面板 继承以定制更多内容
 */
public class PetOption extends WindowBase {
    protected PetBase pet;
    protected JLabel statusLabel1;
    protected JLabel statusLabel2;
    protected Timer updateTimer;
    protected Point dragOffset;
    protected JPanel buttonPanel;
    protected GridBagConstraints buttonGrid;
    protected int buttonsPerRow = 5; // 每行按钮数
    protected Color backgroundColor = new Color(0, 0, 0, 200); // 背景颜色
    protected Color textColor = new Color(250,250,250); // 文本颜色
    public PetOption(PetBase thePet){
        super(
            thePet.getPetData().getName(),
            new Dimension(
                (int)(GUtil.DEFAULT_bodySize.width*2),
                (int)(GUtil.DEFAULT_bodySize.height*1.5)
            )
        );
        this.pet = thePet;
        this.setIconImage(ResourceReader.getResourceAsImageIcon("images/pet.png").getImage());
        initWindow();
        readyToPaint();
    }
    // 初始化窗口
    protected void initWindow(){
        ImageIcon petIcon = ResourceReader.getResourceAsImageIcon("images/icon.png");
        try{
            petIcon = GUtil.scaleImageIcon(
                GUtil.createImageIconFromBytes(JarFileUtil.readByteInJarFile(pet.getJarPath(),"assets/icon.png")),
                GUtil.DEFAULT_textSize*4
            );
        }catch(IOException e){e.printStackTrace();}
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setUndecorated(true);
        this.setAlwaysOnTop(true);
        this.setBackground(backgroundColor);
        this.setLayout(new BorderLayout(5, 5));
        // 创建顶部面板
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // 宠物图标
        JLabel iconLabel = new JLabel(petIcon);
        iconLabel.setPreferredSize(new Dimension(GUtil.DEFAULT_textSize*4, GUtil.DEFAULT_textSize*4));
        topPanel.add(iconLabel, BorderLayout.WEST);
        // 信息面板
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        // 宠物名称
        JLabel nameLabel = new JLabel(pet.getPetData().getName());
        nameLabel.setFont(GUtil.DEFAULT_font.deriveFont(Font.BOLD, GUtil.DEFAULT_textSize));
        nameLabel.setForeground(textColor);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        // 状态信息1
        statusLabel1 = new JLabel();
        statusLabel1.setFont(GUtil.DEFAULT_font.deriveFont(Font.PLAIN, (int)(GUtil.DEFAULT_textSize * 0.9)));
        statusLabel1.setForeground(textColor);
        statusLabel1.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(statusLabel1);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        // 状态信息2
        statusLabel2 = new JLabel();
        statusLabel2.setFont(GUtil.DEFAULT_font.deriveFont(Font.PLAIN, (int)(GUtil.DEFAULT_textSize * 0.8)));
        statusLabel2.setForeground(textColor);
        statusLabel2.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(statusLabel2);
        topPanel.add(infoPanel, BorderLayout.CENTER);
        // 添加拖动区域
        JPanel dragPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        dragPanel.setOpaque(false);
        dragPanel.setPreferredSize(new Dimension(0, 20));
        // 最小化按钮
        JButton minimizeBtn = GUtil.createIconButton("images/button_minus.png", "最小化", 16);
        minimizeBtn.addActionListener(e -> setState(Frame.ICONIFIED));
        dragPanel.add(minimizeBtn);
        // 关闭按钮
        JButton closeBtn = GUtil.createIconButton("images/button_close.png", "关闭", 16);
        closeBtn.addActionListener(e -> closeWindow());
        dragPanel.add(closeBtn);
        topPanel.add(dragPanel, BorderLayout.NORTH);
        this.add(topPanel, BorderLayout.NORTH);
        // 分隔线
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setForeground(new Color(100, 100, 100, 100));
        this.add(separator, BorderLayout.CENTER);
        // 按钮面板
        buttonGrid = new GridBagConstraints();
        buttonGrid.gridx = 0;
        buttonGrid.gridy = 0;
        buttonGrid.insets = new Insets(5,5,5,5);
        buttonGrid.fill = GridBagConstraints.HORIZONTAL;
        buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(buttonPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.add(scrollPane, BorderLayout.SOUTH);
        // 设置拖动行为
        topPanel.addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e){
                dragOffset = e.getPoint();
            }
        });
        topPanel.addMouseMotionListener(new MouseMotionAdapter(){
            @Override
            public void mouseDragged(MouseEvent e){
                Point newLocation = e.getLocationOnScreen();
                newLocation.x -= dragOffset.x;
                newLocation.y -= dragOffset.y;
                setLocation(newLocation);
            }
        });
    }
    // 开始渲染
    protected void readyToPaint(){
        updateStatusText();
        loadButtonsToPanel();
        adjustWindowSize();
    }
    // 加载按钮到面板
    protected void loadButtonsToPanel(){
        buttonPanel.removeAll();
        addButton("images/button_follow.png", "跟随 follow",e->pet.followMouse());
        addButton("images/button_rest.png", "休息 rest",e->pet.doRest());
        addButton("images/button_save.png", "保存 save",e->pet.copyTextFromClipboard());
        addButton("images/button_read.png", "阅读 read",e->pet.readMessageList());
        addButton("images/button_screenshot.png", "截图 screenshot",e->{
            pet.copyScreenImage();
            pet.showMessage("截图已复制 screenshot copied");
        });
        // 重写加载更多......
    }
    // 调整窗口大小
    public void adjustWindowSize(){
        Dimension preferredSize = this.getPreferredSize();
        int width = Math.min(Math.max(preferredSize.width, GUtil.DEFAULT_bodySize.width * 2), GUtil.DEFAULT_bodySize.width * 2);
        int height = Math.min(Math.max(preferredSize.height, GUtil.DEFAULT_bodySize.height), GUtil.DEFAULT_bodySize.height * 2);
        this.setSize(width, height);
        this.revalidate();
    }
    // 更新状态文本
    public void updateStatusText(){
        if(pet.getPlayPetData()==null){return;}
        String statusText1 = String.format("%d+%d | %d+%d",
            pet.getPlayPetData().getGlobalKeyPressCount(),
            pet.getLauncher().getGlobalInputListener().getKeyPressCount(),
            pet.getPlayPetData().getGlobalMouseClickCount(),
            pet.getLauncher().getGlobalInputListener().getMousePressCount()
        );
        String statusText2 = String.format("%d %s",
            pet.getPlayPetData().getAffectionLevel(),
            pet.animationSprite.currentAnimation
        );
        statusLabel1.setText(statusText1);
        statusLabel2.setText(statusText2);
    }
    // 启动状态更新定时器
    public void startStatusUpdate(){
        updateTimer = new Timer(GUtil.GAME_renderTime,e->{
            updateStatusText();
            if(!pet.isVisible()){
                closeWindow();
                updateTimer.stop();
            }
        });
        updateTimer.start();
    }
    // 关闭窗口
    public void closeWindow(){
        if (updateTimer != null){
            updateTimer.stop();
        }
        this.setVisible(false);
        this.dispose();
    }
    // 显示窗口
    public void showWindow(){
        startStatusUpdate();
        Point petLocation = pet.getLocationOnScreen();
        Point windowLocation = new Point(petLocation.x + pet.getWidth() + 5, petLocation.y);
        if (windowLocation.x + this.getWidth() > GUtil.SCREEN_dimension.width){
            windowLocation.x = petLocation.x - this.getWidth() - 5;
        }
        if (windowLocation.y + this.getHeight() > GUtil.SCREEN_dimension.height){
            windowLocation.y = GUtil.SCREEN_dimension.height - this.getHeight();
        }
        this.setLocation(windowLocation);
        this.setVisible(true);
        if(!updateTimer.isRunning()){updateTimer.start();}
    }
    // 添加按钮 从主程序
    protected void addButton(String iconPath,String tooltip,ActionListener listener){
        JButton button = GUtil.createIconButton(iconPath,tooltip,GUtil.DEFAULT_textSize*2);
        button.addActionListener(listener);
        buttonPanel.add(button,buttonGrid);
        buttonGrid.gridx++;
        if(buttonGrid.gridx>buttonsPerRow-1){
            buttonGrid.gridx = 0;
            buttonGrid.gridy++;
        }
    }
    // 添加按钮 从桌宠包
    protected void addButton(ImageIcon icon,String tooltip,ActionListener listener){
        JButton button = GUtil.createIconButton(icon,tooltip,GUtil.DEFAULT_textSize*2);
        button.addActionListener(listener);
        buttonPanel.add(button,buttonGrid);
        buttonGrid.gridx++;
        if(buttonGrid.gridx>buttonsPerRow-1){
            buttonGrid.gridx = 0;
            buttonGrid.gridy++;
        }
    }
}
