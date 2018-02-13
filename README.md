# Interactive-WASFGA
A Java application to solve multiobjective optimization problems in an interactive way using the Interactive WASFGA (iWASFGA) method.

iWASFGA has been developed in Java using the NetBeans (https://netbeans.org/) Integrated Development Environment (IDE). iWASFGA also uses jMetal 4 (http://jmetal.sourceforge.net/), a Java-based framework for multiobjective optimization. To plot Pareto fronts, iWASFGA uses the Gnuplot (http://www.gnuplot.info/).

iWASFGA includes test problems from the ZDT, DTLZ and WFG families, respectively, for which the number of objectives can vary between two and six.

Next figure shows the Graphical User Interface (GUI) and usage of iWASFGA to solve the DTLZ2 test problem with five objective functions.



Next, we explain each section of the iWASFGA's GUI:

- Algorithm's Configuration. There are three parameters to configure: (a) the number of solutions the Decision Maker (DM) would like to compare at the current iteration, (b) the population size, and (c) the number of generations.

- Problem’s Configuration. The multiobjective optimization problem to solve.

- Reference Point. Approximations of the ideal (best) and the nadir (worst) values are provided to the DM in order to let her/him know the ranges of the objective functions. By clicking on each slider and moving it, the DM can set the aspiration level for each objective.

- Solution Process. To generate the number of selected solutions, the DM must click the 'Start' button. If the DM decides to take a new iteration by changing some preference information (the reference point and/or the number of solutions to be generated), (s)he must click the 'Next Iteration' button to generate new solutions.

- Solutions. It shows the objective values of the solutions obtained for the current reference point chosen by the DM.

- Plot for the Problem. It shows the objective vectors of the solutions found and the reference point. It allows the comparison among different solutions. For biobjective optimization problems, they are plotted in R^2. For multiobjective optimization problems with three or more objective functions, we use a value path representation to shown the solutions obtained and the reference point (labelled as RP). We plot each solution by lines that go across different columns which represent the objective function values they reach. The lower and upper ends of each column represent the total values range of each objective function, that is, its ideal and nadir values, respectively.

- Log. This box shows if there has been any error during the execution.





