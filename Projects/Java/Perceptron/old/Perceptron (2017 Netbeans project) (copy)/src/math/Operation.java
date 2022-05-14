package math;
/**f:C->C type interface
<p> ResFracti
<p> Created by Michael Rule on Mon Mar 28 2005.
*/
public interface Operation {
    /**
     * Function wrapper for f:C->C complex maps
     * @param num
     * @return operation applied to num
     */
    public complex execute(complex num);
    /**
     * Indicates weather the function is analytic.
     * @return True if map is analytic, else false.
     */
    boolean is_analytic();
}	
