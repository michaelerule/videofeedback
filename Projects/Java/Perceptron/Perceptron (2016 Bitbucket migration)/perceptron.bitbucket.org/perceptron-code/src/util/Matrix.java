package util;

/**
 * Perceptron
 *
 * @author Michael Everett Rule
 * @URL <http://perceptron.sourceforge.net/>
 */
public class Matrix {

    /**
     * This operation perfoms no checks on the dimensions of A,B but assumes
     * that they are of the correct dimensions for calculation of the matrix
     * product AB A is M x N matrix B is N x P matrix
     */
    public static float[][] multiply(float[][] A, float[][] B) {
        return multiply(A, B, new float[A.length][B[0].length]);
    }

    /**
     * This operation perfoms no checks on the dimensions of A,B but assumes
     * that they are of the correct dimensions for calculation of the matrix
     * product AB. The result is stored in the given result array. A is M x N
     * matrix B is N x P matrix
     */
    public static float[][] multiply(float[][] A, float[][] B, float[][] result) {
        int N = A[0].length;
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                float result_ij = 0;
                for (int r = 0; r < N; r++) {
                    result_ij += A[i][r] * B[r][j];
                }
                result[i][j] = result_ij;
            }
        }
        return result;
    }

    /**
     * This returns a standard rotation NxN rotation matrix of the K1 and K2
     * basis vectors for angle alpha in radians. K1 < K2, K1, K2 elements of [0
     * N-1] N a natural number
     */
    public static float[][] rotation(int N, int K1, int K2, float alpha) {
        float[][] result = new float[N][N];
        for (int i = 0; i < N; i++) {
            result[i][i] = 1;
        }
        result[K1][K1] = result[K2][K2] = (float) StrictMath.cos(alpha);
        result[K2][K1] = -(result[K1][K2] = (float) StrictMath.sin(alpha));
        return result;
    }

    /**
     * This multiplies matrix A by scalar x
     */
    public static float[][] scale(float[][] A, float x) {
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++) {
                A[i][j] *= x;
            }
        }
        return A;
    }

    /**
     * This adds the elements of B to those of A element-wise, A is modified.
     */
    public static float[][] translate(float[][] A, float[][] B) {
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++) {
                A[i][j] += B[i][j];
            }
        }
        return A;
    }

    /**
     * This normalises a matrix
     */
    public static float[][] normalise(float[][] A) {

        float[][] result = new float[A.length][A[0].length];
        float size = 0;

        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++) {
                result[i][0] += A[i][j];
            }
            size += result[i][0] * result[i][0];
        }

        size = (float) StrictMath.sqrt(size);

        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++) {
                result[i][j] = A[i][j] / size;
            }
        }

        return result;
    }

    // /////////////////////////////////////////////////////////////////////////
    // Methods specialised for 3x3 matricies

    /**
     * This adds the elements of B to those of A element-wise, A is modified.
     */
    public static float[][] translate_3x3(float[][] A, float[][] B) {
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

    /**
     * This changes the basis of the 3x3 linear transformation T to that
     * represented by the 3x3 matrix B
     */
    public static float[][] change_basis_3x3(float[][] T, float[][] B) {
        return multiply(B, multiply(T, invert_3x3(B)));
    }

    /**
     * This inverts a 3x3 matrix
     */
    public static float[][] invert_3x3(float[][] a) {
        float a12a01 = a[1][2] * a[0][1], a11a02 = a[1][1] * a[0][2], a21a02 = a[2][1] * a[0][2], a21a12 = a[2][1] * a[1][2], a22a01 = a[2][2] * a[0][1], a22a11 = a[2][2]
                * a[1][1], a20a12 = a[2][0] * a[1][2], a22a10 = a[2][2] * a[1][0], a22a00 = a[2][2] * a[0][0], a20a02 = a[2][0] * a[0][2], a10a02 = a[1][0]
                * a[0][2], a12a00 = a[1][2] * a[0][0], a21a10 = a[2][1] * a[1][0], a20a11 = a[2][0] * a[1][1], a20a01 = a[2][0] * a[0][1], a21a00 = a[2][1]
                * a[0][0], a11a00 = a[1][1] * a[0][0], a10a01 = a[1][0] * a[0][1];

        float det = a[0][0] * (a22a11 - a21a12) - a[1][0] * (a22a01 - a21a02) + a[2][0] * (a12a01 - a11a02);
        return new float[][]{{(a22a11 - a21a12) / det, (a21a02 - a22a01) / det, (a12a01 - a11a02) / det},
                {(a20a12 - a22a10) / det, (a22a00 - a20a02) / det, (a10a02 - a12a00) / det},
                {(a21a10 - a20a11) / det, (a20a01 - a21a00) / det, (a11a00 - a10a01) / det}};
    }

    /**
     * prints a matrix
     */
    public static void print(float[][] A) {
        System.out.print("{ ");
        for (float[] aA : A) {
            System.out.print("[ ");
            for (float anAA : aA) {
                System.out.print(anAA + " ");
            }
            System.out.print("] ");
        }
        System.out.print("}");
    }
}
