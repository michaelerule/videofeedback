package math;
//
//  myRunnable.java
//  ResFracti
//
//  Created by Michael Rule on Mon Mar 28 2005.
//  Copyright (c) 2005 __MyCompanyName__. All rights reserved.
//

/**
 *
 * @author mer49
 */


public interface Operation {

    /**
     *
     * @param num
     * @return
     */
    public complex execute(complex num);

    /**
     *
     * @return
     */
    boolean is_analytic();
}	
