package me.regexmc.statsoverlay;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;
import me.regexmc.statsoverlay.config.ConfigHandler;
import me.regexmc.statsoverlay.utils.Clients;
import me.regexmc.statsoverlay.utils.Multithreading;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends JFrame {
    public static final TreeMap<Integer, String[]> players = new TreeMap<>();
    public static int bringToFrontKey;
    public static ConfigHandler configHandler;
    public static List<String> usernames = new ArrayList<>();
    public static JPanel panel_Main;
    public static JPanel panel_Settings;
    public static JPanel panel_Players;
    public static JTable table_Players;
    public static JScrollPane jScrollPane;
    public static String roamingPath;
    public static Color defaultCellColor;
    public static HashMap<String, Pattern> patternHashMap = new HashMap<String, Pattern>() {{
        //game join or who, resume key listener
        put("WHO", Pattern.compile("ONLINE: (((\\w{2,16}(,|$) ?){8}$)|((\\w{2,16}(,|$) ?){12}$)|((\\w{2,16}(,|$) ?){16}$))"));

        //game start, stop key listener
        put("GAME_START", Pattern.compile("^     Protect your bed and destroy the enemy beds.$"));
        put("PARTY_WARP", Pattern.compile("^Warped [0-9]+ party members to your lobby$")); //double check this
        put("INVALID_PARTY_WARP", Pattern.compile("^You are not currently in a party.$"));
    }};
    private JButton button_Toggle;
    private JComboBox<String> comboBox_Client;
    private JLabel label_APIKey;
    private JPasswordField passwordField_APIKey;
    private JLabel label_BackgroundColor;
    private JTextField textField_BackgroundColor;
    private JLabel label_InvalidAPIKey_Image;
    private Color defaultPasswordFieldBackgroundColor;
    private JSlider slider_Transparency;
    private JButton button_hotKey;
    private boolean waitingForKeyPress = false;

    public Main(String title) {
        SwingUtilities.invokeLater(() -> {
            setTitle(title);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setState(JFrame.NORMAL);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setLayout(new GridBagLayout());

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
            panel_Main = new JPanel();
            panel_Main.setBackground(Color.decode(configHandler.getBackgroundColor()));

            setContentPane(panel_Main);

            panel_Settings = new JPanel();
            panel_Settings.setLayout(new GridLayout());
            panel_Settings.setName("settings");
            Set<AWTKeyStroke> set = new HashSet<>();
            setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, set);

            button_Toggle = new JButton(configHandler.getEnabled() ? "Disable" : "Enable");
            button_Toggle.setBackground(new Color(255, 0, 0));
            button_Toggle.addActionListener(e -> {
                try {
                    toggle();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });

            comboBox_Client = new JComboBox<>(new String[]{"Forge", "Lunar", "BLC"});
            comboBox_Client.setSelectedItem(configHandler.getClient().getName());
            comboBox_Client.setToolTipText("API Key");
            comboBox_Client.addItemListener(e -> {
                try {
                    updateClient();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });

            label_APIKey = new JLabel("API Key: ", SwingConstants.RIGHT);

            passwordField_APIKey = new JPasswordField(configHandler.getKey());
            passwordField_APIKey.setName("settings_apikey");
            passwordField_APIKey.setPreferredSize(new Dimension(88, 64));
            passwordField_APIKey.setToolTipText("Your Hypixel API Key. Press enter to update.");
            passwordField_APIKey.addActionListener(e -> {
                try {
                    updateKey();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });
            defaultPasswordFieldBackgroundColor = passwordField_APIKey.getBackground();
            if (!configHandler.getValidKey()) passwordField_APIKey.setBackground(new Color(255, 112, 112));


            label_InvalidAPIKey_Image = new JLabel("");
            try {
                label_InvalidAPIKey_Image.setIcon(new ImageIcon(getScaledImage(ImageIO.read(ClassLoader.getSystemResource("error.png")), 64, 64)));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            label_InvalidAPIKey_Image.setVisible(!matches(new String(passwordField_APIKey.getPassword()), "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"));

            button_hotKey = new JButton();
            button_hotKey.setText("Hotkey: " + KeyEvent.getKeyText(configHandler.getHotKey()));
            button_hotKey.addActionListener(l -> {
                waitingForKeyPress = true;
                try {
                    updateHotKey();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });

            label_BackgroundColor = new JLabel("Background Color: ", SwingConstants.RIGHT);

            textField_BackgroundColor = new JTextField(configHandler.getBackgroundColor());
            textField_BackgroundColor.setToolTipText("Press enter to update.");
            textField_BackgroundColor.addActionListener(e -> {
                try {
                    updateBackgroundColor();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });

            slider_Transparency = new JSlider();
            slider_Transparency.setMinimum(20);
            slider_Transparency.setMaximum(100);
            slider_Transparency.setValue(configHandler.getOpacity());
            slider_Transparency.addChangeListener(l -> {
                try {
                    updateTransparency();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });

            panel_Settings.add(button_Toggle);
            panel_Settings.add(comboBox_Client);
            panel_Settings.add(label_APIKey);
            panel_Settings.add(passwordField_APIKey);
            panel_Settings.add(label_InvalidAPIKey_Image);
            panel_Settings.add(button_hotKey);
            panel_Settings.add(label_BackgroundColor);
            panel_Settings.add(textField_BackgroundColor);
            panel_Settings.add(slider_Transparency);

            panel_Players = new JPanel(new GridLayout());

            String[] columnNames = {"(NL) {WS} Username", "Level", "Wins", "Losses", "WL", "Kills", "Deaths", "KD", "Final Kills", "Final Deaths", "FKD"};
            String[][] data = {
                    {"", "", "", "", "", "", "", "", "", "", ""}
            };
            table_Players = new JTable(new DefaultTableModel(data, columnNames)) {
                private static final long serialVersionUID = 1L;

                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                    Component component = super.prepareRenderer(renderer, row, column);
                    int rendererWidth = component.getPreferredSize().width;
                    TableColumn tableColumn = getColumnModel().getColumn(column);
                    tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
                    return component;
                }
            };

            table_Players.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table_Players.setRowHeight(table_Players.getRowHeight() * 3);
            table_Players.setFont(new Font(table_Players.getFont().getName(), Font.PLAIN, table_Players.getFont().getSize() * 2));
            table_Players.setRowSelectionAllowed(false);
            defaultCellColor = table_Players.getBackground();

            jScrollPane = new JScrollPane(table_Players);
            jScrollPane.setPreferredSize(new Dimension(1920 - 200, 1080 - panel_Settings.getHeight() - 100));

            panel_Players.add(jScrollPane);

            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 1;
            panel_Main.add(panel_Settings, gridBagConstraints);
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.anchor = GridBagConstraints.PAGE_END;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.gridy = 1;
            panel_Main.add(panel_Players, gridBagConstraints);

            pack();
            setVisible(true);
            try {
                undecorate(this);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
                e1.printStackTrace();
            }
            setOpacity(new Integer(slider_Transparency.getValue()).floatValue() / 100);
        });
    }

    public static void main(String[] args) throws IOException {
        configHandler = new ConfigHandler(Paths.get(""));
        roamingPath = System.getenv("APPDATA");

        Main form = new Main("Bedwars Stats Overlay");

        bringToFrontKey = configHandler.getHotKey();

        FileWatcher fileWatcher = new FileWatcher();
        fileWatcher.start();

        GlobalKeyboardHook keyboardHook = new GlobalKeyboardHook(false);

        //if MC is in admin, it wont work. add option to elevate these perms

        keyboardHook.addKeyListener(new GlobalKeyAdapter() {
            @Override
            public void keyPressed(GlobalKeyEvent event) {
                if (configHandler.getEnabled()) {
                    if (form.waitingForKeyPress) {
                        bringToFrontKey = event.getVirtualKeyCode();
                        form.waitingForKeyPress = false;
                        try {
                            form.updateHotKey();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                        return;
                    }
                    if (!event.isMenuPressed()) {
                        if (event.getVirtualKeyCode() == bringToFrontKey) {
                            Main.bringtoFront();

                        }
                    }
                }
            }

            @Override
            public void keyReleased(GlobalKeyEvent event) {
                if (configHandler.getEnabled()) {
                    if (!form.waitingForKeyPress) {
                        if (!event.isMenuPressed()) {
                            if (event.getVirtualKeyCode() == bringToFrontKey) {
                                Main.bringMinecraftForward();
                            }
                        }
                    }
                }
            }
        });
    }

    public static JSONObject readJsonFromUrl(String url) throws JSONException, IOException {
        InputStream is = new URL(url).openStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        String jsonText = sb.toString();

        return new JSONObject(jsonText);
    }

    public static void updateRowHeights() {
        for (int row = 0; row < table_Players.getRowCount(); row++) {
            int rowHeight = table_Players.getRowHeight();

            for (int column = 0; column < table_Players.getColumnCount(); column++) {
                Component comp = table_Players.prepareRenderer(table_Players.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }

            table_Players.setRowHeight(row, rowHeight);
        }
    }

    private static String getFocusedWindow() {
        HWND focusedWindow = User32.INSTANCE.GetForegroundWindow();
        char[] name = new char[64];
        User32.INSTANCE.GetWindowText(focusedWindow, name, 64);
        return new String(name);
    }

    //mightn't support Laby or 5zig on its own, add separate clients for them
    public static void bringtoFront() {
        String focusedWindowTitle = getFocusedWindow();
        if (focusedWindowTitle.startsWith(configHandler.getClient().getTitle())) {
            HWND hwnd = User32.INSTANCE.FindWindow(null, "Bedwars Stats Overlay"); // stats overlay title
            if (hwnd != null) {
                User32.INSTANCE.ShowWindow(hwnd, 3);
                User32.INSTANCE.SetForegroundWindow(hwnd);
            }
        }
    }

    public static void bringMinecraftForward() {
        String focusedWindowTitle = getFocusedWindow();
        if (focusedWindowTitle.startsWith("Bedwars Stats Overlay")) {
            HWND hwnd = User32.INSTANCE.FindWindow(null, configHandler.getClient().getTitle()); // client title
            if (hwnd != null) {
                User32.INSTANCE.ShowWindow(hwnd, 3);
                User32.INSTANCE.SetForegroundWindow(hwnd);
            }
        }
    }

    private static void undecorate(Frame frame) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field undecoratedField = Frame.class.getDeclaredField("undecorated");
        undecoratedField.setAccessible(true);
        undecoratedField.set(frame, true);
    }

    private void toggle() throws IOException {
        configHandler.setEnabled(!configHandler.getEnabled());
        button_Toggle.setText(configHandler.getEnabled() ? "Disable" : "Enable");
        button_Toggle.setBackground(configHandler.getEnabled() ? new Color(255, 0, 0) : new Color(0, 255, 0));
        configHandler.write();
    }

    private void updateKey() throws IOException {
        String key = new String(passwordField_APIKey.getPassword());
        if (matches(key, "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            label_InvalidAPIKey_Image.setVisible(false);
            configHandler.setKey(key);
            configHandler.setValidKey(true);
            configHandler.write();
            passwordField_APIKey.setBackground(defaultPasswordFieldBackgroundColor);

            Multithreading.runAsync(() -> {
                try {
                    String apiKeyInfoURL = "https://api.hypixel.net/key?key=" + key;
                    JSONObject apiKeyInfoJSON = readJsonFromUrl(apiKeyInfoURL);
                    if (!apiKeyInfoJSON.getBoolean("success")) {
                        JOptionPane.showMessageDialog(null, "Invalid API Key");
                        label_InvalidAPIKey_Image.setVisible(true);
                        passwordField_APIKey.setBackground(new Color(255, 112, 112));
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Invalid API Key");
                    label_InvalidAPIKey_Image.setVisible(true);
                    passwordField_APIKey.setBackground(new Color(255, 112, 112));
                    e.printStackTrace();
                }
            });

        } else {
            configHandler.setValidKey(false);
            configHandler.write();
            label_InvalidAPIKey_Image.setVisible(true);
            passwordField_APIKey.setBackground(new Color(255, 112, 112));
        }
    }

    private void updateClient() throws IOException {
        configHandler.setClient(Clients.getClientFromName(comboBox_Client.getSelectedItem().toString()));
        configHandler.write();
    }

    private void updateBackgroundColor() throws IOException {
        try {
            panel_Main.setBackground(Color.decode(textField_BackgroundColor.getText()));
            configHandler.setBackgroundColor(textField_BackgroundColor.getText());
            configHandler.write();
        } catch (NumberFormatException numberFormatException) {
            //dont update color in config if it is invalid
        }
    }

    private void updateTransparency() throws IOException {
        JFrame.setDefaultLookAndFeelDecorated(true);
        float opacity = new Integer(slider_Transparency.getValue()).floatValue() / 100;
        setOpacity(opacity);
        configHandler.setOpacity(slider_Transparency.getValue());
        configHandler.write();
    }

    private void updateHotKey() throws IOException {
        if (waitingForKeyPress) {
            button_hotKey.setText("Waiting for keystroke");
        } else {
            configHandler.setHotKey(bringToFrontKey);
            configHandler.write();
            button_hotKey.setText("Hotkey: " + KeyEvent.getKeyText(configHandler.getHotKey()));
        }
    }

    private boolean matches(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }

    private Image getScaledImage(Image srcImg, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();

        return resizedImg;
    }
}
