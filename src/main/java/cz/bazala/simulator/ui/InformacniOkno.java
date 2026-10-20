package cz.bazala.simulator.ui;

import cz.bazala.simulator.model.*;
import cz.bazala.simulator.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

public class InformacniOkno extends JPanel {
    private Runnable akcePoZavreni;

    public InformacniOkno(Runnable akcePoZavreni) {
        this.akcePoZavreni = akcePoZavreni;
        setLayout(new GridBagLayout());
        setBackground(new Color(0, 0, 0, 180)); // PoloprÅ¯hlednÃ© ÄernÃ© pozadÃ­

        addMouseListener(new MouseAdapter() {});

        // --- VNITÅ˜NÃ BOX S NÃVODEM ---
        JPanel oknoPanel = new JPanel();
        oknoPanel.setLayout(new BoxLayout(oknoPanel, BoxLayout.Y_AXIS));
        oknoPanel.setBackground(new Color(40, 40, 40));
        oknoPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // NÃ¡pis
        JLabel nadpis = new JLabel("VÃ­tej v Bazala Simulatoru!");
        nadpis.setFont(new Font("Segoe UI", Font.BOLD, 28));
        nadpis.setForeground(Color.WHITE);
        nadpis.setAlignmentX(Component.CENTER_ALIGNMENT);
        oknoPanel.add(nadpis);
        oknoPanel.add(Box.createVerticalStrut(20));

        // Text nÃ¡vodu
        String navod = "<html><div style='text-align: left; width: 400px;'>" +
                "Tady je rychlÃ½ nÃ¡vod jak zmÃ¡knout smÄ›nu jak Bazala:<br><br>" +
                "1. ZboÅ¾Ã­ na pÃ¡su <b>S kÃ³dem (typ 0)</b> staÄÃ­ pouze kliknout a mÃ¡Å¡ vyÅ™eÅ¡Ã­no.<br>" +
                "2. ZboÅ¾Ã­ <b>Bez kÃ³du (peÄivo, zelenina, ovoce, ...)</b> dojede na scanner a zastavÃ­ se.<br>" +
                "3. Na ÄÃ­selnÃ­ku pak musÃ­Å¡ zadat <b>[ID]*[POÄŒET]</b> nebo <b>[ID]*[VÃHA] (ta je vidÄ›t pod ÃºÄtenkou)</b> a stisknout <b>ENTER</b>.<br>" +
                "4. NÃ¡kup zÃ¡kaznÃ­ka konÄÃ­ kliknutÃ­m na krÃ¡snÃ© <b>dÄ›lÃ­tko nÃ¡kupu</b>.<br><br>" +
                "<b>ZÃ¡kaznÃ­k platÃ­ kartou Äi hotovÄ›</b><br>" +
                "<b>Kartou</b> -> staÄÃ­ kliknout na terminÃ¡l a mÃ¡Å¡ hotovo.<br>" +
                "<b>HotovÄ›</b> -> V kase musÃ­Å¡ vrÃ¡tit sprÃ¡vnÃ© mnoÅ¾stvÃ­ (informace vidÃ­Å¡ kdyÅ¾tak v chatu vlevo a nebo na ÃºÄtence).<br><br>" +
                "KdyÅ¾ budeÅ¡ uÅ¾ za vodou, mÅ¯Å¾eÅ¡ nakoupit novÃ© zboÅ¾Ã­, upgrady Äi hudbu, co hojÃ­ rÃ¡ny.<br><br>" +
                "HodnÄ› Å¡tÄ›stÃ­ a co nejmÃ©nÄ› naÅ¡tvanÃ½ch zÃ¡kaznÃ­kÅ¯!</div></html>";
        JLabel textLabel = new JLabel(navod);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        textLabel.setForeground(new Color(220, 220, 220));
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        oknoPanel.add(textLabel);
        oknoPanel.add(Box.createVerticalStrut(30));

        // TlaÄÃ­tko pro start
        JButton btnZavrit = new JButton("RozumÃ­m, jdeme na to!") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(new Color(0, 120, 0));
                else if (getModel().isRollover()) g2.setColor(new Color(0, 180, 0));
                else g2.setColor(new Color(0, 150, 0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnZavrit.setContentAreaFilled(false);
        btnZavrit.setFocusPainted(false);
        btnZavrit.setBorderPainted(false);
        btnZavrit.setForeground(Color.WHITE);
        btnZavrit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnZavrit.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnZavrit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnZavrit.addActionListener(e -> {
            setVisible(false);
            if (this.akcePoZavreni != null) {
                this.akcePoZavreni.run();
            }
        });

        oknoPanel.add(btnZavrit);
        add(oknoPanel);
    }
}