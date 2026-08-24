

Switch to 4 fold striping.

Stripe x lookups? complicates things. 
Does mirroring still work? somehow yes
bits 01 moved to bits 56

leave unpacked x bits true index

but order lookup table in new order

we cannot allow out of bounds operations, they overflow

scan bounds and pack/unpack in original order

convert scanned index to new index

add step to re-order packed bits















