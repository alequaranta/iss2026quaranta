%====================================================================================
% fireflysynch description   
%====================================================================================
dispatch( cellstate, cellstate(X,Y,COLOR) ).
dispatch( synch, changed(SOURCE,SYNCH,TIME) ).
%====================================================================================
context(ctxfirefly, "localhost",  "TCP", "8040").
context(ctxgrid, "127.0.0.1",  "TCP", "8050").
 qactor( creator, ctxfirefly, "it.unibo.creator.Creator").
 static(creator).
  qactor( firefly, ctxfirefly, "it.unibo.firefly.Firefly").
dynamic(firefly). %%Oct2023 
  qactor( sonar_mock, ctxfirefly, "it.unibo.sonar_mock.Sonar_mock").
 static(sonar_mock).
  qactor( griddisplay, ctxgrid, "external").
