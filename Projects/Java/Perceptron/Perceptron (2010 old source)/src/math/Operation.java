package math;
//
//  myRunnable.java
//  ResFracti
//
//  Created by Michael Rule on Mon Mar 28 2005.
//  Copyright (c) 2005 __MyCompanyName__. All rights reserved.
//


public interface Operation {
	public complex execute(complex num);

    boolean is_analytic();
}	
