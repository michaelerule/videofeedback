package util;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/* Matrix.java
 * Created on March 18, 2007, 12:40 PM
 *
 * This class handles basic operations on floating point matricies
 */

/**
 * @author Michael Everett Rule
 */
public class Matrix {
    
    
    /** Multiply two matrices. This does not check dimensions.
     * @param A M x N matrix
     * @param B N x P matrix
     * @return 
     */
    public static float[][] multiply(float [][] A, float [][] B) {
        return multiply(A, B, new float[A.length][B[0].length]);
    }
    
    
    /** Multiply two matrices storing result in provided target array. This does
     *  not check dimensions.
     * @param A M x N matrix
     * @param B N x P matrix
     * @param R M x P matrix
     * @return
     */
    public static float[][] multiply(float [][] A, float [][] B, float [][] R) {
        int N = A[0].length;
        for (int i = 0; i < R.length; i++)
            for (int j = 0; j < R[i].length; j++) {
                float result_ij = 0;
                for (int r = 0; r < N; r++)
                    result_ij += A[i][r] * B[r][j];
                R[i][j] = result_ij;
            }
        return R;
    }
    
    
    /** Generate an NxN rotation matrix of the K1 and K2 basis vectors for 
     * angle alpha in radians. 
     * @param N  Dimension of matrix
     * @param K1 First axis to rotate (rotate from)
     * @param K2 Second axis to rotate (rotate to)
     * @param alpha Angle to rotate
     * @return 
     **/
    public static float[][] rotation(int N, int K1, int K2, float alpha) {
        float [][] result = new float[N][N];
        for (int i = 0; i < N; i++)
            result[i][i] = 1;
        result[K1][K1] =   result[K2][K2] = (float)cos(alpha);
        result[K2][K1] = -(result[K1][K2] = (float)sin(alpha));
        return result;
    }
    
    /**
     *
     * @param elements
     * @return
     */
    @SuppressWarnings("ManualArrayToCollectionCopy")
    public static float[][] diag(float... elements) {
        int N = elements.length;
        float [][] result = new float[N][N];
        for (int i=0; i<N; i++) result[i][i] = elements[i];
        return result;
    }
    
    
    /** This multiplies matrix A by scalar x
     * @param A
     * @param x
     * @return  */
    public static float[][] scale(float[][] A, float x) {
        for (float[] A1 : A) for (int j = 0; j < A1.length; j++) A1[j] *= x;
        return A;
    }
    
    
    /** This adds the elements of B to those of A element-wise,
     *  A is modified.
     * @param A
     * @param B
     * @return 
     */
    public static float[][] translate(float [][] A, float [][] B) {
        for (int i = 0; i < A.length; i++)
            for (int j = 0; j < A[i].length; j++)
                A[i][j] += B[i][j];
        return A;
    }
    
    
    /** This normalises a matrix
     * @param A
     * @return  */
    public static float[][] normalise(float [][] A) {
        
        float [][] result = new float[A.length][A[0].length];
        float size = 0;
        
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++)
                result[i][0] += A[i][j];
            size += result[i][0] * result[i][0];
        }
        
        size = (float)StrictMath.sqrt(size);
        
        for (int i = 0; i < A.length; i++)
            for (int j = 0; j < A[i].length; j++)
                result[i][j] = A[i][j] / size;
        
        return result;
    }
    
    
    
    ///////////////////////////////////////////////////////////////////////////
    //Methods specialised for 3x3 matricies
    
    /** This adds the elements of B to those of A element-wise,
     *  A is modified.
     * @param A
     * @param B
     * @return 
     */
    public static float[][] translate_3x3(float [][] A, float [][] B) {
        A[0][0] += B[0][0];
        A[0][1] += B[0][1];
        A[0][2] += B[0][2];
        A[1][0] += B[1][0];
        A[1][1] += B[1][1];
        A[1][2] += B[1][2];
        A[2][0] += B[2][0];
        A[2][1] += B[2][1];
        A[2][2] += B[2][2];
        return A;
    }
    
    
    /** This changes the basis of the 3x3 linear transformation T to that
     *  represented by the 3x3 matrix B
     * @param T
     * @param B
     * @return  */
    public static float[][] change_basis_3x3(float[][] T, float[][] B) {
        return multiply(B, multiply(T, invert_3x3(B)));
    }
    
    /**
     * Landerman 1975 3x3 matrix product with 23 multiplications
     * @param A
     * @param B
     * @return 
     * @throws java.lang.Exception 
     */
    public static float[][] multiply_3x3_Laderman(float[][] A, float [][] B) throws Exception {
        throw new Exception("There's a bug somewhere, don't use.");
        /*
        float a11 = A[0][0];
        float a12 = A[0][1];
        float a13 = A[0][2];
        float a21 = A[1][0];
        float a22 = A[1][1];
        float a23 = A[1][2];
        float a31 = A[2][0];
        float a32 = A[2][1];
        float a33 = A[2][2];
        float b11 = B[0][0];
        float b12 = B[0][1];
        float b13 = B[0][2];
        float b21 = B[1][0];
        float b22 = B[1][1];
        float b23 = B[1][2];
        float b31 = B[2][0];
        float b32 = B[2][1];
        float b33 = B[2][2];
        float P01 = (a11-a12-a13+a21-a22-a32-a33) * (-b22);
        float P02 = (a11+a21) * (b12+b22);
        float P03 = (a22) * (b11-b12+b21-b22-b23+b31-b33);
        float P04 = (-a11-a21+a22) * (-b11+b12+b22);
        float P05 = (-a21+a22) * (-b11+b12);
        float P06 = (a11) * (-b11);
        float P07 = (a11+a31+a32) * (b11-b13+b23);
        float P08 = (a11+a31) * (-b13+b23);
        float P09 = (a31+a32) * (b11-b13);
        float P10 = (a11+a12-a13-a22+a23+a31+a32) * (b23);
        float P11 = (a32) * (-b11+b13+b21-b22-b23-b31+b32);
        float P12 = (a13+a32+a33) * (b22+b31-b32);
        float P13 = (a13+a33) * (-b22+b32);
        float P14 = (a13) * (b31);
        float P15 = (-a32-a33) * (-b31+b32);
        float P16 = (a13+a22-a23) * (b23-b31+b33);
        float P17 = (-a13+a23) * (b23+b33);
        float P18 = (a22-a23) * (b31-b33);
        float P19 = (a12) * (b21);
        float P20 = (a23) * (b32);
        float P21 = (a21) * (b13);
        float P22 = (a31) * (b12);
        float P23 = (a33) * (b33);
        float r1 = (-P06+P14+P19-a11*b11-a12*b21-a13*b31);
        float r2 = (P01-P04+P05-P06-P12+P14+P15-a11*b12-a12*b22-a13*b32);
        float r3 = (-P06-P07+P09+P10+P14+P16+P18-a11*b13-a12*b23-a13*b33);
        float r4 = (P02+P03+P04+P06+P14+P16+P17-a21*b11-a22*b21-a23*b31);
        float r5 = (P02+P04-P05+P06+P20-a21*b12-a22*b22-a23*b32);
        float r6 = (P14+P16+P17+P18+P21-a21*b13-a22*b23-a23*b33);
        float r7 = (P06+P07-P08+P11+P12+P13-P14-a31*b11-a32*b21-a33*b31);
        float r8 = (P12+P13-P14-P15+P22-a31*b12-a32*b22-a33*b32);
        float r9 = (P06+P07-P08-P09+P23-a31*b13-a32*b23-a33*b33);
        return new float[][] {{r1,r2,r3},{r4,r5,r6},{r7,r8,r9}};
        */
    }
    
    /**
     * Marakov's 22 multiplication 3x3 matrix product for COMMUTATIVE
     * matrices only. 
     * http://www.mathnet.ru/links/
     *      4c474953a5b87c840ad1d468b86738f8/zvmmf4056.pdf
     * https://www.sciencedirect.com/science/article/abs/pii/
     *      004155538690203X?via%3Dihub
     * @param A
     * @param B
     * @return 
     */
    public static float[][] multiply_commutative_3x3(float[][] A, float [][] B) {
        float k1 = A[0][0];
        float b1 = A[0][1];
        float c1 = A[0][2];
        float k2 = A[1][0];
        float b2 = A[1][1];
        float c2 = A[1][2];
        float k3 = A[2][0];
        float b3 = A[2][1];
        float c3 = A[2][2];
        float a1 = B[0][0];
        float a2 = B[0][1];
        float a3 = B[0][2];
        float k4 = B[1][0];
        float k5 = B[1][1];
        float k6 = B[1][2];
        float k7 = B[2][0];
        float k8 = B[2][1];
        float k9 = B[2][2];
        float M1 = (a3+c1-c2)*(k1+k7-k8+k9);
        float M2 = (a2+b1+b2)*(k2-k4+k5-k6);
        float M3 = (a2+b1+b3)*(k3-k4+k5-k6);
        float M4 = (a3-c2-c3)*(k3-k7+k8-k9);
        float M5 = (a1-c1+c2)*k1;
        float M6 = (a1+b1+b2)*k2;
        float M7 = (a1+b1+b3+c2+c3)*k3;
        float M8 = a2*(k1+k4-k5+k6);
        float M9 = a3*(k2+k7-k8+k9);
        float M10 = b1*k4;
        float M11 = c2*k7;
        float M12 = (c1-c2)*(k1+k7);
        float M13 = (b1+b2)*(k4-k2);
        float M14 = (a1+b1)*(k4-k5+k6);
        float M15 = b2*k6;
        float M16 = (a3-c2)*(k7-k8+k9);
        float M17 = c2*k8;
        float M18 = (b3-c2-c3)*k6;
        float M19 = (c1+c3-b1-b3)*k8;
        float M20 = (b1+b3)*(k4-k3+k6+k8);
        float M21 = (c2+c3)*(k3+k6-k7+k8);
        float M22 = (c2+c3-b1-b3)*(k6+k8);
        float r1 = M5+M10+M11+M12;
        float r2 = M8+M10-M14+M17-M18+M19-M22;
        float r3 = M1-M11-M12-M16+M17-M18+M19-M22;
        float r4 = M6-M10+M11+M13;
        float r5 = M2-M10+M13+M14+M15+M17;
        float r6 = M9-M11+M15-M16+M17;
        float r7 = M7-M10-M11+M20-M21+M22;
        float r8 = M3-M10+M14-M17+M18+M20+M22;
        float r9 = M4+M11+M16-M17+M18+M21;
        return new float[][] {{r1,r2,r3},{r4,r5,r6},{r7,r8,r9}};
    }
    
    
    /** This inverts a 3x3 matrix
     * @param a
     * @return 
     */
    public static float[][] invert_3x3(float [][] a) {
        float   a12a01 = a[1][2] * a[0][1],
                a11a02 = a[1][1] * a[0][2],
                a21a02 = a[2][1] * a[0][2],
                a21a12 = a[2][1] * a[1][2],
                a22a01 = a[2][2] * a[0][1],
                a22a11 = a[2][2] * a[1][1],
                a20a12 = a[2][0] * a[1][2],
                a22a10 = a[2][2] * a[1][0],
                a22a00 = a[2][2] * a[0][0],
                a20a02 = a[2][0] * a[0][2],
                a10a02 = a[1][0] * a[0][2],
                a12a00 = a[1][2] * a[0][0],
                a21a10 = a[2][1] * a[1][0],
                a20a11 = a[2][0] * a[1][1],
                a20a01 = a[2][0] * a[0][1],
                a21a00 = a[2][1] * a[0][0],
                a11a00 = a[1][1] * a[0][0],
                a10a01 = a[1][0] * a[0][1];
        
        float det = a[0][0] * (a22a11 - a21a12)
                  - a[1][0] * (a22a01 - a21a02)
                  + a[2][0] * (a12a01 - a11a02);
        return new float[][] {
            {(a22a11 - a21a12) / det, 
                     (a21a02 - a22a01) / det, 
                     (a12a01 - a11a02) / det},
            {(a20a12 - a22a10) / det, 
                     (a22a00 - a20a02) / det, 
                     (a10a02 - a12a00) / det},
            {(a21a10 - a20a11) / det, 
                     (a20a01 - a21a00) / det, 
                     (a11a00 - a10a01) / det}
        };
    }
    
    
    /** prints a matrix
     * @param A */
    public static void print(float [][] A) {
        System.out.println("{ ");
        for (float[] A1 : A) {
            System.out.print("  [ ");
            for (int j = 0; j < A1.length; j++) 
                System.out.print(A1[j] + " ");
            System.out.println("] ");
        }
        System.out.println("}");
    }
    
    
    
    ///////////////////////////////////////////////////////////////////////////
    //CONVIENIENT STATIC METHODS
    /** Calculate the float distance between two float points.
     * @param a
     * @param b
     * @return 
     */
    public static final float distance(float[] a, float[] b) {
        float 
        x = a[0] - b[0],
        y = a[1] - b[1],
        z = a[2] - b[2];
        return (float) (sqrt(x * x + y * y + z * z));
    }

    /** Matrix multiplication simplified for linear transformation of low
     *  percision 3D points of small value
     * @param A
     * @param P
     * @return  
     */
    public static final float[] multiply_3x3_point(float[][] A, float[] P) {
        return multiply_3x3_point(A, P, new float[3]);
    }

    /** Matrix multiplication simplified for linear transformation of low
     *  percision 3D points of small value
     * @param A
     * @param P
     * @param result
     * @return  
     */
    public static final float[] multiply_3x3_point(float[][] A, float[] P, float[] result) {
        result[0] = (float) (A[0][0] * P[0] + A[0][1] * P[1] + A[0][2] * P[2]);
        result[1] = (float) (A[1][0] * P[0] + A[1][1] * P[1] + A[1][2] * P[2]);
        result[2] = (float) (A[2][0] * P[0] + A[2][1] * P[1] + A[2][2] * P[2]);
        return result;
    }

    /**
     * 
     * @param P
     * @param dr
     * @return 
     */
    public static final float[] scale_point(float[] P, double dr) {
        P[0] *= dr;
        P[1] *= dr;
        P[2] *= dr;
        return P;
    }
}
