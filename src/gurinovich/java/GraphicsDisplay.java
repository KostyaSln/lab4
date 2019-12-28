package gurinovich.java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;

public class GraphicsDisplay extends JPanel
{
    private Double[][] GraphicsData;
    private Double[][] OldMinMax = new Double[2][2];

    private boolean ShowAxis = true;
    private boolean ShowPoints = true;
    private boolean Turned = false;
    private boolean SecondShown = true;

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    private double scale;

    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    private BasicStroke SelectionStroke;

    private Font axisFont;
    private Font CoordinatesFont;

    private MyMouseAdapter adapter;

    private double a;

    public GraphicsDisplay()
    {
        setBackground(Color.WHITE);

        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[]{10, 3, 10, 3, 10, 3, 3, 3, 3, 3, 3, 3 }, 0.0f);
        SelectionStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[]{10, 10}, 0.0f);
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);

        axisFont = new Font("Serif", Font.BOLD, 36);
        CoordinatesFont = new Font("Serif", Font.BOLD,12);

        adapter = new MyMouseAdapter();
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    public void showGraphics(Double[][] graphicsData)
    {
        this.GraphicsData = graphicsData;
        repaint();
    }

    public Double[][] getGraphicsData()
    {
        return GraphicsData;
    }

    public void setShowAxis(boolean showAxis)
    {
        this.ShowAxis = showAxis;
        repaint();
    }

    public void setShowPoints(boolean showPoints)
    {
        this.ShowPoints = showPoints;
        repaint();
    }

    public void setTurned(boolean Turned)
    {
        this.Turned = Turned;
        repaint();
    }

    public void setSecondShown(boolean SecondShown)
    {
        this.SecondShown = SecondShown;
        repaint();
    }

    public void paintComponent(Graphics g)//////////////////////////////////////////////////////////////////////////////
    {
        super.paintComponent(g);

        a = 0;

        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();

        if (GraphicsData == null || GraphicsData.length == 0)
            return;

        minX = GraphicsData[0][0];
        maxX = GraphicsData[GraphicsData.length-1][0];
        minY = GraphicsData[0][1];
        maxY = minY;

        for (int i = 1; i < GraphicsData.length; i++)
        {
            if (GraphicsData[i][1] < minY)
                minY = GraphicsData[i][1];

            if (GraphicsData[i][1] > maxY)
                maxY = GraphicsData[i][1];
        }

        if (Selected)///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            Zoom();

        double scaleX = getSize().getWidth() / (maxX - minX);//обоих колбасит
        double scaleY = getSize().getHeight() / (maxY - minY);//

        if (Turned)
        {
            a = ( getWidth() - getHeight() ) / 2;
            scaleX = getSize().getHeight() / (maxX - minX);
            scaleY = getSize().getWidth() / (maxY - minY);

            AffineTransform tr = AffineTransform.getRotateInstance(Math.PI / 2, getWidth() / 2, getHeight() / 2);

            canvas.setTransform(tr);
        }

        scale = Math.min(scaleX, scaleY);

        if (Turned)
        {
            if (scale == scaleX)
            {
                double yIncrement = (getSize().getWidth() / scale - (maxY - minY)) / 2;

                maxY += yIncrement;
                minY -= yIncrement;
            }

            if (scale == scaleY)
            {
                double xIncrement = (getSize().getHeight() / scale - (maxX - minX)) / 2;

                maxX += xIncrement;
                minX -= xIncrement;
            }
        }
        else
        {
            if (scale == scaleX)
            {
                double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;

                maxY += yIncrement;
                minY -= yIncrement;
            }

            if (scale == scaleY)
            {
                double xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 2;

                maxX += xIncrement;
                minX -= xIncrement;
            }
        }

        if (ShowAxis)
            paintAxis(canvas);

        paintGraphics(canvas);

        if (ShowPoints)
            paintMarkers(canvas);

        if (Selecting)
            Selection(canvas);


        if (!OnPointPressed)
            for (int i = 0; i < GraphicsData.length; i++)
            {
                OnPoint = true;
                Point2D.Double point = xyToPoint(GraphicsData[i][0], GraphicsData[i][1]);
                if (point.getX() >= MousePresentX - 5 && point.getX() <= MousePresentX + 5)
                    if (point.getY() >= MousePresentY - 5 && point.getY() <= MousePresentY + 5)
                    {
                        NumberCoordinatesToShow = i;
                        ShowingCoordinates(canvas);
                        break;
                    }
                OnPoint = false;
            }

        if (OnPointPressed)
            ShowingCoordinates(canvas);

        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);


    }



    protected void paintGraphics(Graphics2D canvas)
    {
        canvas.setStroke(graphicsStroke);

        canvas.setColor(Color.RED);

        GeneralPath graphics = new GeneralPath();
        for (int i = 0; i < GraphicsData.length; i++)
        {
            Point2D.Double point = xyToPoint(GraphicsData[i][0], GraphicsData[i][1]);
            if (i > 0)
                graphics.lineTo(point.getX(), point.getY());
            else
                graphics.moveTo(point.getX(), point.getY());

        }

        canvas.draw(graphics);
    }

    protected void paintMarkers(Graphics2D canvas)
    {
        canvas.setStroke(markerStroke);

        canvas.setColor(Color.RED);

        canvas.setPaint(Color.RED);

        for (Double[] point: GraphicsData)
        {
            int sum = 0, y = point[1].intValue();

            while (y != 0)
            {
                sum += y % 10;
                y /= 10;
            }

            if (sum < 10)
                canvas.setColor(Color.GREEN);

            Ellipse2D.Double marker = new Ellipse2D.Double();

            Point2D.Double center = xyToPoint(point[0], point[1]);

            Point2D.Double corner = shiftPoint(center, 5, 5);

            marker.setFrameFromCenter(center, corner);

            Point2D.Double UpShift = shiftPoint(center, 0, -5);
            Line2D.Double lineU = new Line2D.Double(center, UpShift);

            Point2D.Double DnShift = shiftPoint(center, 0, 5);
            Line2D.Double lineD = new Line2D.Double(center, DnShift);

            Point2D.Double LShift = shiftPoint(center, -5, 0);
            Line2D.Double lineL = new Line2D.Double(center, LShift);

            Point2D.Double RShift = shiftPoint(center, 5, 0);
            Line2D.Double lineR = new Line2D.Double(center, RShift);

            canvas.draw(marker);
            canvas.draw(lineU);
            canvas.draw(lineD);
            canvas.draw(lineL);
            canvas.draw(lineR);

            if (sum < 10)
                canvas.setColor(Color.RED);
        }
    }

    protected void paintAxis(Graphics2D canvas)
    {
        canvas.setStroke(axisStroke);

        canvas.setColor(Color.BLACK);

        canvas.setPaint(Color.BLACK);

        canvas.setFont(axisFont);

        FontRenderContext context = canvas.getFontRenderContext();

        if (minX <= 0.0 && maxX >= 0.0)
        {
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));

            GeneralPath arrow = new GeneralPath();

            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());

            arrow.lineTo(arrow.getCurrentPoint().getX()+5, arrow.getCurrentPoint().getY()+20);

            arrow.lineTo(arrow.getCurrentPoint().getX()-10, arrow.getCurrentPoint().getY());

            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);

            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);

            canvas.drawString("y", (float)labelPos.getX() + 10, (float)(labelPos.getY() - bounds.getY()));
        }

        if (minY <= 0.0 && maxY >= 0.0)
        {
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));

            GeneralPath arrow = new GeneralPath();

            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());

            arrow.lineTo(arrow.getCurrentPoint().getX()-20, arrow.getCurrentPoint().getY()-5);

            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY()+10);
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);

            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);

            canvas.drawString("x", (float)(labelPos.getX() - bounds.getWidth() - 10), (float)(labelPos.getY() + bounds.getY()));
        }
    }

    protected Point2D.Double xyToPoint(double x, double y)
    {
        double deltaX = x - minX;
        double deltaY = maxY - y;

        return new Point2D.Double(deltaX * scale + a, deltaY * scale - a);
    }

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY)
    {
        Point2D.Double dest = new Point2D.Double();

        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);

        return dest;
    }

    private void Zoom()
    {
        OldMinMax[0][0] = minX;
        OldMinMax[0][1] = maxY;
        OldMinMax[1][0] = maxX;
        OldMinMax[1][1] = minY;
        minX = SelectedMinMax[0][0];
        maxX = SelectedMinMax[1][0];
        minY = SelectedMinMax[1][1];
        maxY = SelectedMinMax[0][1];
    }

    private boolean Selecting = false, Selected = false;
    private void Selection(Graphics2D canvas)
    {
        canvas.setStroke(SelectionStroke);
        canvas.setColor(Color.GRAY);

        Rectangle2D.Double rect;

        if (MousePresentX <= MouseFirstX && MousePresentY <= MouseFirstY)
            rect = new Rectangle2D.Double(MouseFirstX, MouseFirstY, 0, 0);
        else if (MousePresentX <= MouseFirstX)
            rect = new Rectangle2D.Double(MouseFirstX, MouseFirstY, 0, MousePresentY - MouseFirstY);
        else if (MousePresentY <= MouseFirstY)
            rect = new Rectangle2D.Double(MouseFirstX, MouseFirstY, MousePresentX - MouseFirstX, 0);
        else
            rect = new Rectangle2D.Double(MouseFirstX, MouseFirstY, MousePresentX - MouseFirstX, MousePresentY - MouseFirstY);

        canvas.draw(rect);
    }

    private void ShowingCoordinates(Graphics2D canvas)
    {
        canvas.setFont(CoordinatesFont);
        canvas.setColor(Color.BLACK);
        canvas.drawString("( " + GraphicsData[NumberCoordinatesToShow][0] + ", " + GraphicsData[NumberCoordinatesToShow][1] + " )", (int)MousePresentX + 10, (int)MousePresentY);
    }

    private int MouseFirstX = 0, MouseFirstY = 0, NumberCoordinatesToShow = 0;
    private double MousePresentX = 0, MousePresentY = 0, PrevPresentY = 0;
    private boolean OnPoint = false, OnPointPressed = false;
    private double[][] SelectedMinMax = new double[2][2];

    private class MyMouseAdapter extends MouseAdapter
    {
        int PressedButton = 0;

        @Override
        public void mousePressed(MouseEvent e)
        {
            PressedButton = e.getButton();
            MouseFirstX = e.getX();
            MouseFirstY = e.getY();
            PrevPresentY = MousePresentY;
            if (OnPoint)
                OnPointPressed = true;
            repaint();
        }
        @Override
        public void mouseReleased(MouseEvent e)
        {
            if (Selecting)//////////////////////////////////////////////////////////////////////////////////////////////////////////////
            {
                SelectedMinMax[0][0] = minX + (MouseFirstX - a) / scale;
                SelectedMinMax[0][1] = maxY - (MouseFirstY + a) / scale;
                SelectedMinMax[1][0] = minX + (MousePresentX - a) / scale;
                SelectedMinMax[1][1] = maxY - (MousePresentY + a) / scale;
                Selected = true;
                Selecting = false;
            }
            PressedButton = 0;
            MouseFirstX = 0;
            MouseFirstY = 0;
            OnPointPressed = false;

            repaint();
        }
        @Override
        public void mouseClicked(MouseEvent e)
        {
            if (e.getButton() == MouseEvent.BUTTON3)
                Selected = false;

        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            MousePresentX = e.getX();
            MousePresentY = e.getY();

            if (PressedButton == MouseEvent.BUTTON1)
            {
                if (OnPointPressed)
                {
                    GraphicsData[NumberCoordinatesToShow][1] += (-MousePresentY + PrevPresentY + a) / scale;
                    PrevPresentY = MousePresentY;
                }
                else
                {
                    Selecting = true;
                }
            }
            repaint();
        }
        @Override
        public void mouseMoved(MouseEvent e)
        {
            MousePresentX = e.getX();
            MousePresentY = e.getY();
            //Point2D.Double point = xyToPoint(MousePresentX, MousePresentY);
           // System.out.println(point.getX() + " " + point.getY() + " " + MousePresentX + " " + MousePresentY);
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {

        }
        @Override
        public void mouseExited(MouseEvent e)
        {

        }
    }
}
