%====================================================================================
% fireflysynch description   
%====================================================================================
dispatch( cellstate, cellstate(X,Y,COLOR) ).
dispatch( synch, changed(SOURCE,TIME) ).
%====================================================================================
context(ctxfirefly, "localhost",  "TCP", "8040").
context(ctxgrid, "127.0.0.1",  "TCP", "8050").
 qactor( creator, ctxfirefly, "it.unibo.creator.Creator").
 static(creator).
  qactor( firefly_queen, ctxfirefly, "it.unibo.firefly_queen.Firefly_queen").
 static(firefly_queen).
  qactor( firefly, ctxfirefly, "it.unibo.firefly.Firefly").
dynamic(firefly). %%Oct2023 
  qactor( griddisplay, ctxgrid, "external").
