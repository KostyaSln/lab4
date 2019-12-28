package gurinovich.java;

import javafx.scene.chart.Axis;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;

public class Main extends JFrame
{
    private JFileChooser fileChooser = null;

    private GraphicsDisplay display = new GraphicsDisplay();

    private JCheckBoxMenuItem ShowAxis;
    private JCheckBoxMenuItem ShowPoints;
    private JCheckBoxMenuItem TurnRight;
    private JCheckBoxMenuItem SecondGraphic;

    private JMenuItem Save;

    private boolean FileLoaded = false, AxisShown = true, PointsShown = true, SecondShown = false, Turned = false;

    public Main()
    {
        super("idk");
        setSize(640, 480);
        Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation( (kit.getScreenSize().width - 640) / 2, (kit.getScreenSize().height - 480) / 2 );

//////////
        JMenuBar Menu = new JMenuBar();

        JMenu File = new JMenu("File");

        JMenuItem Open = new JMenuItem("Open");

        Open.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                if (fileChooser == null)
                {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }

                if (fileChooser.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION)
                {
                    OpenGraphics(fileChooser.getSelectedFile());
                    fileChooser.setSelectedFile(new File(""));
                }
            }
        });

        File.add(Open);

        Save = new JMenuItem("Save");

        Save.setEnabled(false);

        Save.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                if (fileChooser.showSaveDialog(Main.this) == JFileChooser.APPROVE_OPTION)
                {
                    SaveGraphics();
                    fileChooser.setSelectedFile(new File(""));
                }
            }
        });

        File.add(Save);

            Menu.add(File);

        JMenu Graphic = new JMenu("Graphic");

        ShowAxis = new JCheckBoxMenuItem("Show axis");

        ShowAxis.setState(true);

        ShowAxis.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                display.setShowAxis(!AxisShown);
                AxisShown = !AxisShown;
            }
        });

        Graphic.add(ShowAxis);

        ShowPoints = new JCheckBoxMenuItem("Show points");

        ShowPoints.setState(true);

        ShowPoints.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                display.setShowPoints(!PointsShown);
                PointsShown = !PointsShown;
            }
        });

        Graphic.add(ShowPoints);

        TurnRight = new JCheckBoxMenuItem("Turn right");

        TurnRight.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                display.setTurned(!Turned);
                Turned = !Turned;
            }
        });

        Graphic.add(TurnRight);

        SecondGraphic = new JCheckBoxMenuItem("Show second");

        SecondGraphic.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                display.setSecondShown(!SecondShown);
                SecondShown = !SecondShown;
            }
        });

        //Graphic.add(SecondGraphic);

        Graphic.addMenuListener(new GraphicsMenuListener());

            Menu.add(Graphic);

        setJMenuBar(Menu);
//////////

        getContentPane().add(display, BorderLayout.CENTER);
    }

    private void OpenGraphics(File file)
    {
        try
        {
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            Double[][] GraphicsData = new Double[in.available() / (Double.SIZE / 8) / 2][];

            int i = 0;
            while (in.available() > 0)
            {
                Double x = in.readDouble();
                Double y = in.readDouble();
                GraphicsData[i++] = new Double[]{ x, y };
            }

            if (GraphicsData != null && GraphicsData.length > 0)
            {
                FileLoaded = true;
                display.showGraphics(GraphicsData);
            }

            in.close();
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }

    private void SaveGraphics()
    {
        fileChooser.setSelectedFile(new File(fileChooser.getSelectedFile().getAbsolutePath() + ".txt"));

        try
        {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(fileChooser.getSelectedFile()));
            Double[][] Data = display.getGraphicsData();

            for (int i = 0; i < Data.length; i++)
                for(int j = 0; j < 2; j++)
                {
                    out.writeDouble(Data[i][j]);
                }
        }
        catch (FileNotFoundException e)
        {}
        catch (IOException e)
        {}
    }

    public static void main(String[] args)
    {
        Main frame = new Main();
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private class GraphicsMenuListener implements MenuListener
    {
        @Override
        public void menuSelected(MenuEvent menuEvent)
        {
            ShowAxis.setEnabled(FileLoaded);
            ShowPoints.setEnabled(FileLoaded);
            TurnRight.setEnabled(FileLoaded);
            SecondGraphic.setEnabled(FileLoaded);
            Save.setEnabled(FileLoaded);
        }

        @Override
        public void menuDeselected(MenuEvent menuEvent)
        {

        }

        @Override
        public void menuCanceled(MenuEvent menuEvent)
        {

        }
    }


}
