package org.unibl.etf.mdp.supplier.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class RequestsFrame extends GeneralFrame {

	private static final long serialVersionUID = 1L;

	public RequestsFrame(String title) {
		super(title);
		setTitle("Requests");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Requests Content Goes Here");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
	}

}
