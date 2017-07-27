package perceptron;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static java.lang.Math.*;

public class Controller implements AbstractController {

    private Object object;
    private Point C;
    private ArrayList<RadialControl> controls;
    private int activeControl;
    private RadialControl active = null;
    private Color ACTIVECOLOR = new Color(0x8800ff00, true);
    private Color INACTIVECOLOR = new Color(0x2200ff00, true);

    public Controller(Object o, Point C) {
        object = o;
        controls = new ArrayList<RadialControl>();
        this.C = C;
        activeControl = -1;
    }

    public void addControl(String name, double min, double max, double init, double r) {
        int R = (int) (r + .5);
        RadialControl rc = null;
        try {
            rc = new RadialControl(min, max, init, R,
                    object.getClass().getMethod(setterName(name), Double.TYPE), name);
        } catch (Exception e1) {
            try {
                rc = new RadialControl(min, max, init, R,
                        object.getClass().getMethod(setterName(name), Float.TYPE), name);
            } catch (Exception e2) {
                try {
                    rc = new RadialControl(min, max, init, R,
                            object.getClass().getMethod(setterName(name), Integer.TYPE), name);
                } catch (Exception e3) {
                }
            }
        }
        if (rc != null) {
            controls.add(rc);
        }

        if (activeControl < 0) {
            activeControl = 1;
            active = rc;
        }
    }

    String setterName(String s) {
        String result = "set";
        boolean caps = true;
        for (char c : s.toCharArray()) {
            if (caps) {
                result += Character.toUpperCase(c);
                caps = false;
            } else {
                if (c == '_') {
                    caps = true;
                } else {
                    result += c;
                }
            }
        }
        return result;
    }

    public void stepControl(int i) {
        activeControl = (activeControl + i + controls.size()) % +controls.size();
        active = controls.get(activeControl);
    }

    public void stepActive(double dR) {
        if (active != null) {
            active.moveDR(dR);
        }
    }

    public void stepFrame(double dt) {
        for (RadialControl R : controls) {
            R.stepValue(dt);
        }
    }

    public void paint(Graphics g) {
        if (g == null) {
            return;
        }
        for (RadialControl R : controls) {
            R.paint(g);
        }
    }

    public class RadialControl {

        int radius;
        double value;
        double damped_value;
        double min;
        double max;
        Point Co, Z, dZ;
        Method M;
        String name;

        public RadialControl(double min, double max, double v, int r,
                Method M, String name) {
            this.radius = r;
            this.value = damped_value = v;
            this.min = min;
            this.max = max;
            this.name = name;
            this.M = M;
            Co = new Point(C.x - radius / 2, C.y - radius / 2);
            fixTheta();
        }

        public void paint(Graphics G) {
            G.setColor(this == active ? ACTIVECOLOR : INACTIVECOLOR);
            G.drawOval(Co.x, Co.y, radius, radius);
            G.drawOval(dZ.x, dZ.y, 8, 8);
            if (this == active) {
                G.drawOval(Z.x, Z.y, 8, 8);
                G.drawString(name + " : " + value, 0, 20);
            }
        }

        public void moveDV(double dV) {
            //System.out.println("moveDV("+dV+")");
            //System.out.println("x : "+x+" y : "+y+" value : "+value);
            double nv = value + dV;
            if (nv < min) {
                nv = min;
            }
            if (nv > max) {
                nv = max;
            }
            value = nv;
            //System.out.println("min : "+min+" max : "+max+" value : "+value);
            try {
                M.invoke(object, (float) value);
            } catch (Exception ex) {
                System.err.println("failed " + M + ".invoke(" + object + ", " + value + ")");
                ex.printStackTrace();
            }
            fixTheta();
            //System.out.println("x : "+x+" y : "+y+" value : "+value);
        }

        public void moveDR(double dR) {
            moveDV(dR * (max - min) / (2 * PI));
        }

        void fixTheta() {
            double theta = value / (max - min) * 2 * PI;
            double dtheta = damped_value / (max - min) * 2 * PI;
            Z = new Point((int) (.5 + C.x + .5 * radius * cos(theta) - 4), (int) (.5 + C.y + .5 * radius * sin(theta) - 4));
            dZ = new Point((int) (.5 + C.x + .5 * radius * cos(dtheta) - 4), (int) (.5 + C.y + .5 * radius * sin(dtheta) - 4));
        }

        public double value() {
            return value;
        }

        public double dampedValue() {
            return damped_value;
        }

        public double stepValue(double dr) {
            damped_value += dr * (value - damped_value);
            fixTheta();
            return damped_value;
        }
    }
}
