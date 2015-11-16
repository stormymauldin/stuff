!create r : Rec

?r.fac(4)

??r.fac(2)

-- the following triggers a core dump in the blackdown jdk1.3
-- ?r.recurse()
