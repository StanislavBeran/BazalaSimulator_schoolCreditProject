import javax.swing.*;
import java.awt.*;
import java.net.URL; // Přidáno pro načítání z resources

public class Menu extends JFrame {
    private CardLayout karty;
    private JPanel hlavniContainer;

    public Menu() {
        setTitle("Bazala Simulator");
        setSize(1280, 720);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. OPRAVA: Relativní cesta k ikoně
        try {
            URL iconUrl = getClass().getResource("/logoAplikace.png");
            if (iconUrl != null) {
                ImageIcon img = new ImageIcon(iconUrl);
                setIconImage(img.getImage());
            } else {
                System.out.println("Ikona aplikace (/logoAplikace.png) nebyla nalezena.");
            }
        } catch (Exception e) {
            System.err.println("Chyba při načítání ikony: " + e.getMessage());
        }

        karty = new CardLayout();
        hlavniContainer = new JPanel(karty);

        BackgroundPanel hlavniMenu = vytvorHlavniMenu();
        NovaHraMenu novaHraObrazovka = new NovaHraMenu(this);
        NacistHruMenu nacistHruMenu = new NacistHruMenu(this);
        NastaveniMenu nastaveniMenu = new NastaveniMenu(this);
        BazalaSimulator bazalaSimulator = new BazalaSimulator(this);

        hlavniContainer.add(hlavniMenu, "HLAVNI_MENU");
        hlavniContainer.add(novaHraObrazovka, "NOVA_HRA");
        hlavniContainer.add(nacistHruMenu, "NACIST_HRU");
        hlavniContainer.add(bazalaSimulator, "BAZALA_SIMULATOR");

        // POZNÁMKA: Odkomentuj si toto, pokud chceš, aby fungovalo tlačítko "Nastavení"
        hlavniContainer.add(nastaveniMenu, "NASTAVENI");

        setContentPane(hlavniContainer);
    }

    private BackgroundPanel vytvorHlavniMenu() {
        // 2. OPRAVA: Relativní cesta pro pozadí
        BackgroundPanel panel = new BackgroundPanel("/pozadi.png");
        panel.setLayout(new GridLayout());

        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setOpaque(false);

        // 3. OPRAVA: Relativní cesta k logu a prázdný catch blok
        try {
            URL logoUrl = getClass().getResource("/logo.png");
            if (logoUrl != null) {
                ImageIcon logoIcon = new ImageIcon(logoUrl);
                Image img = logoIcon.getImage();
                double ratio = (double) img.getHeight(null) / img.getWidth(null);
                int newWidth = 400;
                int newHeight = (int) (newWidth * ratio);
                JLabel logoLabel = new JLabel(new ImageIcon(img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH)));
                logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                menuContainer.add(logoLabel);
            } else {
                System.out.println("Logo (/logo.png) nebylo nalezeno.");
            }
        } catch (Exception e) {
            System.err.println("Chyba při načítání loga: " + e.getMessage());
        }

        menuContainer.add(Box.createVerticalStrut(50)); // Mezera pod logem

        String[] texty = {"Začít novou hru", "Načíst hru", "Nastavení", "Exit"};
        for (String text : texty) {
            JButton btn = vytvorTlacitko(text);
            btn.addActionListener(e -> {
                String cmd = e.getActionCommand();
                switch (cmd) {
                    case "Začít novou hru":
                        zobrazObrazovku("NOVA_HRA");
                        break;
                    case "Načíst hru":
                        zobrazObrazovku("NACIST_HRU");
                        break;
                    case "Nastavení":
                        zobrazObrazovku("NASTAVENI");
                        break;
                    case "Exit":
                        System.exit(0);
                }
            });
            menuContainer.add(btn);
            menuContainer.add(Box.createVerticalStrut(15));
        }
        panel.add(menuContainer);
        return panel;
    }

    public void zobrazObrazovku(String nazev) {
        karty.show(hlavniContainer, nazev);
    }

    // Tvoje metoda pro tlačítko zůstává nezměněna, je super!
    public static JButton vytvorTlacitko(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(40, 40, 40));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(70, 70, 70));
                } else {
                    g2.setColor(new Color(50, 50, 50, 220));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(300, 50));
        btn.setPreferredSize(new Dimension(300, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    // 4. OPRAVA: BackgroundPanel nyní používá getResource místo File
    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String path) {
            // Načtení přes URL umožňuje fungování i zabalené v JARu
            URL imgUrl = getClass().getResource(path);
            if (imgUrl != null) {
                this.backgroundImage = new ImageIcon(imgUrl).getImage();
            } else {
                System.err.println("Nenalezen obrázek pozadí: " + path);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                // Pokud obrázek chybí, použijeme barvu
                setBackground(new Color(30, 30, 30));
            }
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            new Menu().setVisible(true);
        });
    }
}