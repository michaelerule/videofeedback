package rendered3D;

import java.util.ArrayList;
import java.awt.Color;
import rendered3D.Object3D.RotateablePoint3D;

/**
 *
 * @author mer49
 */
public class Tree3D1 extends Object3D 
{
    private final static int [][][] rgba = fillRgba();

    private static int [][][] fillRgba() {
        int [][][] result = new int[40][][];
        for (int i = 0; i < result.length; i++) {
            int ii = i+1;
            result[i] = new int[(int)(ii*Math.PI)][3];
            for (int j = 0; j < result[i].length; j++) {
                double shade = Math.abs((double)j/result[i].length*2-.5)*(-128);
                result[i][j][0] = (int)(Math.min(255,Math.max(0,190+shade)));
                result[i][j][1] = (int)(Math.min(255,Math.max(0,190+shade)));
                result[i][j][2] = (int)(Math.min(255,Math.max(0,80+shade)));
            }
        }
        return result;
    }

    private class Branch {

        public double length, phi, theta, width;
        public Point3D start, end;

        public RotateablePoint3D [] base, top;

        @SuppressWarnings("unchecked")
        public Branch [] branch(
            double alpha, 
            double beta, 
            double dr, 
            double dw, 
            boolean wireframe, 
            boolean filled, 
            boolean cylinder,
            ArrayList p, 
            ArrayList r, 
            ArrayList m, 
            ArrayList s) 
        {        
            double newwidth  = width  * dw;
            double newradius = length * dr;

            RotateablePoint3D branch1 = new RotateablePoint3D(
                    newradius * Math.cos(alpha),
                    newradius * Math.sin(alpha),0);

            alpha = Math.PI - alpha;

            RotateablePoint3D branch2 = new RotateablePoint3D(
                    newradius * Math.cos(alpha),
                    newradius * Math.sin(alpha),0);

            RotateablePoint3D connect1 = new RotateablePoint3D(0,newwidth,newwidth);
            RotateablePoint3D connect2 = new RotateablePoint3D(0,newwidth,-newwidth);

            top = new RotateablePoint3D[4];

            double toptheta = -Math.PI/4;
            for (int i = 0; i < 4; i++) { 
                top[i] = new RotateablePoint3D(width * Math.cos(toptheta),0,width * Math.sin(toptheta));
                toptheta += Math.PI/2;
            }

            RotateablePoint3D rotate[] = { branch1, branch2, connect1, connect2, top[0], top[1], top[2], top[3]};    

            double    gamma = Math.PI/2 - phi,
                cosgamma = Math.cos(gamma),
                singamma = Math.sin(gamma),
                delta = -theta,
                cosdelta = Math.cos(delta),
                sindelta = Math.sin(delta),
                cosbeta = Math.cos(beta),
                sinbeta = Math.sin(beta);

            for (RotateablePoint3D rotate1 : rotate) {
                double x = cosbeta * rotate1.x + sinbeta * rotate1.z;
                double y = rotate1.y;
                double z = cosbeta * rotate1.z - sinbeta * rotate1.x;
                rotate1.x = x;
                rotate1.y = y;
                rotate1.z = z;
                x = cosgamma * rotate1.x + singamma * rotate1.y;
                y = cosgamma * rotate1.y - singamma * rotate1.x;
                z = rotate1.z;
                rotate1.x = x;
                rotate1.y = y;
                rotate1.z = z;
                x = cosdelta * rotate1.x + sindelta * rotate1.z;
                y = rotate1.y;
                z = cosdelta * rotate1.z - sindelta * rotate1.x;
                rotate1.x = x;
                rotate1.y = y;
                rotate1.z = z;
            }

            double branch1theta = Math.atan2(branch1.z, branch1.x);
            double branch1phi   = Math.atan2(branch1.y, branch1.z/Math.sin(branch1theta));
            double branch2theta = Math.atan2(branch2.z, branch2.x);
            double branch2phi   = Math.atan2(branch2.y, branch2.z/Math.sin(branch2theta));

            for (RotateablePoint3D rotate1 : rotate) {
                rotate1.x += end.getx();
                rotate1.y += end.gety();
                rotate1.z += end.getz();
            }

            int baseindex = 0;
            double startingbasedistance = Math.sqrt(
                Math.pow(base[0].x-top[0].x,2)+
                Math.pow(base[0].y-top[0].y,2)+
                Math.pow(base[0].z-top[0].z,2));

            for (int i = 1; i < 4; i++) {
                double distance = Math.sqrt(
                Math.pow(base[i].x-top[0].x,2)+
                Math.pow(base[i].y-top[0].y,2)+
                Math.pow(base[i].z-top[0].z,2));
                if (distance < startingbasedistance) {
                    startingbasedistance = distance;
                    baseindex = i;
                }
            }
            if (filled) {
                for (int topindex = 0; topindex < 4; topindex++) 
                {
                    int topindex2  = (topindex  + 1) % 4;
                    int baseindex2 = (baseindex + 1) % 4; 

                    Point3D [] temp3 = {base[baseindex], base[baseindex2], top[topindex]};
                    Point3D [] temp4 = {base[baseindex2], top[topindex2], top[topindex]};
                    if (wireframe) {
                        s.add(new Outline3D(temp3,randc()));
                        s.add(new Outline3D(temp4,randc()));
                    } else {
                        s.add(new Form3D(temp3,randc()));
                        s.add(new Form3D(temp4,randc()));
                    }

                    baseindex = (baseindex + 1)%4;
                    p.add(top[topindex]);
                    r.add(top[topindex]);
                    m.add(top[topindex].clone2());
                }    
                Point3D [] temp1 = {top[1],top[2],connect1};
                Point3D [] temp2 = {top[3],top[0],connect2};
                if (wireframe) {
                    s.add(new Outline3D(temp1, randc()));
                    s.add(new Outline3D(temp2, randc()));
                } else {
                    s.add(new Form3D(temp1, randc()));
                    s.add(new Form3D(temp2, randc()));
                }

                p.add(connect1);
                r.add(connect1);
                m.add(connect1.clone2());
                p.add(connect2);
                r.add(connect2);
                m.add(connect2.clone2());
            } else {
                if (cylinder) 
                    if (wireframe)
                        s.add(new TaperedStem3D(start,end,randc(),width,newwidth));
                    else {
                        int W = (int)((width+newwidth)/2);
                        if (W >= 0 && W < rgba.length)
                             s.add(new ShadedCylinder3D(start,end,rgba[W],W));
                        else s.add(new ShadedCylinder3D(start,end,new Color(140,140,40),W));
                    }
                else  
                    s.add(wireframe 
                        ? new Line3D(start,end,randc())
                        : new ThickLine3D(start,end,
                                new Color(140,140,50),
                                (width+newwidth)/2.0));
                p.add(end);
                r.add(end);
                m.add(end.clone2());
            }

            RotateablePoint3D [] branch1base = {top[0], top[1], connect1, connect2};
            RotateablePoint3D [] branch2base = {top[2], top[3], connect2, connect1};

            Branch resultingBranch1 = new Branch(
                end,branch1,newradius,branch1base,branch1phi,branch1theta,newwidth);
            Branch resultingBranch2 = new Branch(
                end,branch2,newradius,branch2base,branch2phi,branch2theta,newwidth);

            Branch [] result = {resultingBranch1, resultingBranch2};
            return result;
        }

        @SuppressWarnings("unchecked")
        public Branch append(
            double alpha, 
            double beta, 
            double dr, 
            double dw, 
            boolean wireframe, 
            boolean filled, 
            boolean cylinder,
            ArrayList p, 
            ArrayList r, 
            ArrayList m, 
            ArrayList s)
        {        
            double newwidth  = width  * dw;
            double newradius = length * dr;

            RotateablePoint3D branch1 = new RotateablePoint3D(
                newradius * Math.cos(alpha),
                newradius * Math.sin(alpha),0);

            top   = new RotateablePoint3D[4];

            double toptheta = -Math.PI/4;
            for (int i = 0; i < 4; i++) 
            { 
                top[i] = new RotateablePoint3D(width * Math.cos(toptheta),0,width * Math.sin(toptheta));
                toptheta += Math.PI/2;
            }

            RotateablePoint3D [] rotate = 
            {branch1, top[0], top[1], top[2], top[3]};    

            double gamma = Math.PI/2 - phi,
                cosgamma = Math.cos(gamma),
                singamma = Math.sin(gamma),
                delta = -theta,
                cosdelta = Math.cos(delta),
                sindelta = Math.sin(delta),
                cosbeta = Math.cos(beta),
                sinbeta = Math.sin(beta);

            for (RotateablePoint3D rotate1 : rotate) {
                double x = cosbeta * rotate1.x + sinbeta * rotate1.z;
                double y = rotate1.y;
                double z = cosbeta * rotate1.z - sinbeta * rotate1.x;
                rotate1.x = x;
                rotate1.y = y;
                rotate1.z = z;
                x = cosgamma * rotate1.x + singamma * rotate1.y;
                y = cosgamma * rotate1.y - singamma * rotate1.x;
                z = rotate1.z;
                rotate1.x = x;
                rotate1.y = y;
                rotate1.z = z;
                x = cosdelta * rotate1.x + sindelta * rotate1.z;
                y = rotate1.y;
                z = cosdelta * rotate1.z - sindelta * rotate1.x;
                rotate1.x = x;
                rotate1.y = y;
                rotate1.z = z;
            }

            double branch1theta = Math.atan2(branch1.z, branch1.x);
            double branch1phi   = Math.atan2(branch1.y, branch1.z/Math.sin(branch1theta));

            for (RotateablePoint3D rotate1 : rotate) {
                rotate1.x += end.getx();
                rotate1.y += end.gety();
                rotate1.z += end.getz();
            }

            int baseindex = 0;
            double startingbasedistance = Math.sqrt(
                Math.pow(base[0].x-top[0].x,2)+
                Math.pow(base[0].y-top[0].y,2)+
                Math.pow(base[0].z-top[0].z,2));

            for (int i = 1; i < 4; i++) 
            {
                double distance = Math.sqrt(
                Math.pow(base[i].x-top[0].x,2)+
                Math.pow(base[i].y-top[0].y,2)+
                Math.pow(base[i].z-top[0].z,2));
                if (distance < startingbasedistance) {
                    startingbasedistance = distance;
                    baseindex = i;
                }
            }

            if (filled) {
                for (int topindex = 0; topindex < 4; topindex++) 
                {
                    int topindex2  = (topindex  + 1) % 4;
                    int baseindex2 = (baseindex + 1) % 4; 

                    Point3D [] temp3 = {base[baseindex], base[baseindex2], top[topindex]};
                    Point3D [] temp4 = {base[baseindex2], top[topindex2], top[topindex]};
                    if (wireframe) {
                        s.add(new Outline3D(temp3,randc()));
                        s.add(new Outline3D(temp4,randc()));
                    } else {
                        s.add(new Form3D(temp3,randc()));
                        s.add(new Form3D(temp4,randc()));
                    }

                    baseindex = (baseindex + 1)%4;
                    p.add(top[topindex]);
                    r.add(top[topindex]);
                    m.add(top[topindex].clone2());
                }
            } else {
                if (cylinder) 
                    if (wireframe) s.add(new TaperedStem3D(start,end,randc(),width,newwidth));
                    else {
                        int W = (int)((width+newwidth)/2);
                        if (W >= 0 && W < rgba.length)
                             s.add(new ShadedCylinder3D(start,end,rgba[W],W));
                        else s.add(new ShadedCylinder3D(start,end,new Color(140,140,40),W));
                    }
                else 
                    if (wireframe) s.add(new Line3D(start,end,randc()));
                    else s.add(new ThickLine3D(start,end,new Color(140,140,50),(width+newwidth)/2.0));
                p.add(end);
                r.add(end);
                m.add(end.clone2());
            }

            return new Branch(end,branch1,newradius,top,branch1phi,branch1theta,newwidth);
        }

        @SuppressWarnings("unchecked")
        public void cap(
            ArrayList p, 
            ArrayList r, 
            ArrayList m, 
            ArrayList s,
            boolean filled, 
            boolean leaf, 
            boolean wireframe, 
            boolean cylinder)
        {
            p.add(end);
            r.add(end);
            m.add(end.clone2());

            p.add(start);
            r.add(start);
            m.add(start.clone2());

            if (leaf) 
            {                    
                RotateablePoint3D [] rotate = {
                    new RotateablePoint3D( length*3,length*8,0), 
                    new RotateablePoint3D(-length*3,length*8,0),};    

                double gamma = Math.PI/2 - phi,
                    cosgamma = Math.cos(gamma),
                    singamma = Math.sin(gamma),
                    delta = -theta,
                    cosdelta = Math.cos(delta),
                    sindelta = Math.sin(delta);

                double x,y,z;
                for (RotateablePoint3D rotate1 : rotate) {        
                    //double x = cosbeta * rotate[i].x + sinbeta * rotate[i].z ;
                    //double y = rotate[i].y;
                    //double z = cosbeta * rotate[i].z - sinbeta * rotate[i].x;
                    //rotate[i].x = x;
                    //rotate[i].y = y;
                    //rotate[i].z = z;
                    x = cosgamma * rotate1.x + singamma * rotate1.y;
                    y = cosgamma * rotate1.y - singamma * rotate1.x;
                    z = rotate1.z;
                    rotate1.x = x;
                    rotate1.y = y;
                    rotate1.z = z;
                    x = cosdelta * rotate1.x + sindelta * rotate1.z;
                    y = rotate1.y;
                    z = cosdelta * rotate1.z - sindelta * rotate1.x;
                    rotate1.x = x;
                    rotate1.y = y;
                    rotate1.z = z;
                    rotate1.x += end.getx();
                    rotate1.y += end.gety();
                    rotate1.z += end.getz();
                    p.add(rotate1);
                    r.add(rotate1);
                    m.add(rotate1.clone2());
                }
                Point3D [] temp = {end,rotate[0],rotate[1]};
                if (wireframe)
                    s.add(new Outline3D(temp,randg()));
                else 
                    s.add(new Form3D(temp,randg()));
            } 

            if (filled) {
                for (int i = 0; i < 4; i++)  {
                    int j = (i+1) % 4;
                    Point3D [] temp = {base[i],base[j],end};
                    if (wireframe) s.add(new Outline3D(temp,randg()));
                    else s.add(new Form3D(temp,randg()));
                }
            } 
            else if (cylinder) 
                if (wireframe)
                    s.add(new TaperedStem3D(start,end,randg(),width,0));else {
                    int W = (int)(width);
                    if (W >= 0 && W < rgba.length)
                         s.add(new ShadedCylinder3D(start,end,rgba[W],W));
                    else s.add(new ShadedCylinder3D(start,end,new Color(140,140,40),W));
                }
            else 
                if (wireframe) s.add(new Line3D(start,end,randg()));
                else  s.add(new ThickLine3D(start,end,randg(),width));
            //if (leaf) s.add(new Sphere3D(end,9,Color.GREEN));
        }

        private Branch(Point3D s, Point3D e, double r, RotateablePoint3D [] b, double p, double t, double w) 
        {
            start = s; end = e; length = r; base = b; phi = p; theta = t; width = w;
        }

        @SuppressWarnings("unchecked")
        public Branch(
            Branch stem, 
            double r, 
            double alpha, 
            double beta,
            double w, 
            boolean wireframe,
            ArrayList points, 
            ArrayList rotateablepoints, 
            ArrayList masterpoints, 
            ArrayList shapes)
        {
            double x0 = r * Math.cos(alpha),
                y1 = r * Math.sin(alpha),
                x1 =   Math.cos(beta) * x0,
                z2 = - Math.sin(beta) * x0,
                gamma = Math.PI/2 - stem.phi,
                cosgamma = Math.cos(gamma),
                singamma = Math.sin(gamma),
                x2 = cosgamma * x1 + singamma * y1,
                y3 = cosgamma * y1 - singamma * x1,
                delta = -stem.theta,
                cosdelta = Math.cos(delta),
                sindelta = Math.sin(delta),
                x3 =   cosdelta * x2 + sindelta * z2,
                z3 = - sindelta * x2 + cosdelta * z2;

            start = stem.end;
            end = new RotateablePoint3D(
                x3 + stem.end.getx(),
                y3 + stem.end.gety(),
                z3 + stem.end.getz());

            length = r;
            theta = Math.atan2(z3, x3);
            phi   = Math.atan2(y3, z3/Math.sin(theta));
            width = w;

            base = new RotateablePoint3D [4];
            top  = new RotateablePoint3D [4];
            double basetheta = 0;
            for (int i = 0; i < 4; i++) {
                double     
                    ax1 = width*Math.cos(basetheta),
                    az2 = width*Math.sin(basetheta),
                    agamma = Math.PI/2 - phi,
                    acosgamma = Math.cos(agamma),
                    asingamma = Math.sin(agamma),
                    ax2 =   acosgamma * ax1,
                    ay3 = - asingamma * ax1,
                    adelta = - theta,
                    acosdelta = Math.cos(adelta),
                    asindelta = Math.sin(adelta),
                    ax3 =   acosdelta * ax2 + asindelta * az2,
                    az3 = - asindelta * ax2 + acosdelta * az2;

                base[i] //= stem.top[i];
                = new RotateablePoint3D(
                    start.getx() + ax3,
                    start.gety() + ay3,
                    start.getz() + az3);
                top[i]  = new RotateablePoint3D(
                    end.getx() + ax3,
                    end.gety() + ay3,
                    end.getz() + az3);
                basetheta += Math.PI / 2;
            }
            for (int i = 0; i < 4; i++) {
                points.add(top[i]);
                rotateablepoints.add(top[i]);
                masterpoints.add(top[i].clone2());
                points.add(base[i]);
                rotateablepoints.add(base[i]);
                masterpoints.add(base[i].clone2());
                int j = (i+1)%4;
                Point3D [] temp = 
                {base[i], top[i], top[j], base[j]};
                if (wireframe) shapes.add(new Outline3D(temp,randc()));
                else shapes.add(new Form3D(temp,randc()));
            }
            if (wireframe) shapes.add(new Outline3D(base,randc()));
            else shapes.add(new Form3D(base,randc()));
        }

        @SuppressWarnings("unchecked")
        public Branch(Point3D s, 
            double l, double t, double p, double w, boolean wireframe, boolean filled, boolean cylinder,
                ArrayList points, 
                ArrayList rotateablepoints, 
                ArrayList masterpoints, 
                ArrayList shapes) {
                start = s;
                length = l;
                theta = t;
                width = w;
                phi = p;
                end = new RotateablePoint3D(
                    start.getx() + length * Math.cos(phi) * Math.cos(theta),
                    start.gety() + length * Math.sin(phi),
                    start.getz() + length * Math.cos(phi) * Math.sin(theta));

                base = new RotateablePoint3D [4];
                top  = new RotateablePoint3D [4];
                double basetheta = 0;
                for (int i = 0; i < 4; i++) {
                    double     
                        x1 = width*Math.cos(basetheta),
                        z2 = width*Math.sin(basetheta),
                    //values rotated pi/2-phi degrees around z axis to align with stem
                        gamma = Math.PI/2 - phi,
                        cosgamma = Math.cos(gamma),
                        singamma = Math.sin(gamma),
                        x2 =   cosgamma * x1,
                        y3 = - singamma * x1,
                    //values rotated theta degrees about the y axis to align completely with stem
                        delta = - theta,
                        cosdelta = Math.cos(delta),
                        sindelta = Math.sin(delta),
                        x3 =   cosdelta * x2 + sindelta * z2,
                        z3 = - sindelta * x2 + cosdelta * z2;

                    base[i] = new RotateablePoint3D(
                        start.getx() + x3,
                        start.gety() + y3,
                        start.getz() + z3);
                    top[i]  = new RotateablePoint3D(
                        end.getx() + x3,
                        end.gety() + y3,
                        end.getz() + z3);
                    basetheta += Math.PI / 2;
                }
                if (filled)  {
                    for (int i = 0; i < 4; i++) {
                        points.add(base[i]);
                        rotateablepoints.add(base[i]);
                        masterpoints.add(base[i].clone2());
                    }
                    if (wireframe) shapes.add(new Outline3D(base,randc()));
                    else shapes.add(new Form3D(base,randc()));
                } else {
                    points.add(start);
                    rotateablepoints.add(start);
                    masterpoints.add(start.clone2());
                }
        }
    }

    /**
     *
     * @return
     */
    public static Color randc() {
        return new Color (
            (int)(100+Math.random()*110),
            (int)(100+Math.random()*110),
            (int)(Math.random()    *80));
    }

    /**
     *
     * @return
     */
    public static Color randg() {
        return new Color (
            (int)(Math.random()*110),
            (int)(100+Math.random()*110),
            (int)(Math.random()    *80));
    }

    /**
     *
     * @param r
     * @param dr
     * @param rr
     * @param w
     * @param dw
     * @param alpha
     * @param alphar
     * @param beta
     * @param betar
     * @param iterations
     * @param drawleaf
     * @param wireframe
     * @param filled
     * @param cylinder
     */
    public Tree3D1(
        double r, 
        double dr, 
        double rr, 
        double w, 
        double dw, 
        double alpha, 
        double alphar, 
        double beta, 
        double betar,
        int iterations,
        boolean drawleaf,
        boolean wireframe,
        boolean filled,
        boolean cylinder) {

        alpha = alpha/360*Math.PI*2;
        beta  = beta /360*Math.PI*2;
        dr /= 100;
        dw /= 100;
        alphar /= 50;
        betar /= 50;

        ArrayList 
            lines = new ArrayList(), 
            points = new ArrayList(), 
            masterpoints = new ArrayList(),
            rotateablepoints = new ArrayList();

        RotateablePoint3D start = new RotateablePoint3D(0,-150,0);

        Branch [] a1 = {new Branch(start, r, 0 , Math.PI/2, w,wireframe,filled,cylinder,points,rotateablepoints,masterpoints,lines)};
        Branch [] a2 = new Branch[2];
        int c1 = 1;
        double articulation = .1;
        for (int iter = 0; iter < iterations; iter++)
        {
            int counter = 0;
            for (int i = 0; i < c1; i++)
            {
                double a = alpha + alphar*(Math.random() - .5)*Math.PI;
                double b = beta  + betar *(Math.random() - .5)*Math.PI;
                double rdr = dr * (1 + (Math.random() - .5) * rr / 50.0);
                for (int q = 0; q < 8; q++) {
                        a1[i] = a1[i].append(Math.PI/2-(Math.random()-.5)*articulation,b,rdr,dw,wireframe,filled,cylinder,points,rotateablepoints,masterpoints,lines);
                }

                Branch [] newbranches = 
                a1[i].branch(a,b,rdr,dw,wireframe,filled,cylinder,points,rotateablepoints,masterpoints,lines);
                a2[counter++] = newbranches[0];
                a2[counter++] = newbranches[1];    
            }
            c1 = counter;
            a1 = a2;
            a2 = new Branch [a2.length * 2];
            articulation += Math.random()/4.;
        }
        for (int i = 0; i < c1; i++) {
            double b = beta  + betar *(Math.random() - .5)*Math.PI;
            double rdr = dr * (1 + (Math.random() - .5) * rr / 50.0);
            for (int q = 0; q < 8; q++) {
                a1[i] = a1[i].append(Math.PI/2-(Math.random()-.5)*articulation,b,rdr,dw,wireframe,filled,cylinder,points,rotateablepoints,masterpoints,lines);
            }
            a1[i].cap(points,rotateablepoints,masterpoints,lines,filled,drawleaf,wireframe,cylinder);
        }
        finalize(points, rotateablepoints, masterpoints,new ArrayList(),lines);
    }

    /**
     *
     */
    public Tree3D1() {
        ArrayList 
            lines = new ArrayList(), 
            points = new ArrayList(), 
            masterpoints = new ArrayList(),
            rotateablepoints = new ArrayList();

        RotateablePoint3D start = new RotateablePoint3D(0,-150,0);

        Branch [] a1 = {new Branch(start, 100, 0 , Math.PI/2, 15 ,false,true,false,points,rotateablepoints,masterpoints,lines)};
        Branch [] a2 = new Branch[2];

        for (int iter = 0; iter < 9; iter++)
        {
            int counter = 0;
            for (Branch a11 : a1) {
                double d = Math.random()*2*Math.PI;
                Branch[] newbranches = a11.branch(1, d, .7, .7, false, true, false, points, rotateablepoints, masterpoints, lines);
                a2[counter++] = newbranches[0];
                a2[counter++] = newbranches[1];    
            }
            a1 = a2;
            a2 = new Branch [a2.length * 2];
        }

        for (Branch a11 : a1) {
            a11.cap(points, rotateablepoints, masterpoints, lines, true, false, false, false);
            //lines.add(new Circle3D(a1[i].end,4,Color.GREEN));
        }

        super.finalize(points, rotateablepoints, masterpoints,new ArrayList(),lines);
    }

}
