// Chase Harwell
// 4614181
// EEE 4334 Fall 2021

import java.util.*;
import java.io.*;

public class FlowCompute
{
	private static int xbar_vars = 0;
	private static int bdd_vars = 0;

	private static boolean[] bddTableResults;
	private static boolean[] xbarTableResults;

	public FlowCompute(String firstFile, String optional)
	{
		int secondFile = 0;
		String file = " ";

		while (secondFile < 2)
		{

			int endIndex = 0;
			char end = ' ';
			if (secondFile == 0)
			{
				file = firstFile;
				endIndex = file.length()-1;
				end = file.charAt(endIndex);
			}
			else
			{
				file = optional;
				endIndex = file.length()-1;
				end = file.charAt(endIndex);
			}


			Scanner inB = new Scanner(System.in);

			// BDD Solving
			if (end == 'd')
			{

				// Read the BDD file and print it to the console
				int[][] bddArray = readBDD(file);
				printGraph(bddArray);

				int[] variable_instances = new int[bdd_vars];

				// Retrieve an instance of the BDD from the user
				for (int i = 0; i < bdd_vars; i++)
				{
					System.out.print("Enter the value for x" + (i+1) + ": ");
					int inputedVar = inB.nextInt();
					if ((inputedVar == 0) || (inputedVar == 1))
					{
						variable_instances[i] = inputedVar;
					}
					else
					{
						System.out.println("Error: Value must be set to 0 or 1. Try again.");
						i--;
					}
				}

				// Solve the BDD given the literal instances from the user
				boolean solved = solveBDD(0, bddArray, variable_instances);

				// Print whether the oupput is true or false
				if(solved)
				{
					System.out.println("Instance of BDD output is 1");
				}
				else
				{
					System.out.println("Instance of BDD output is 0");
				}


				// Print the truth table of the input BDD
				// This involves testing every single possible combination of inputs
				bddTableResults = printTruthTable(end, bddArray, bdd_vars);

				// Determines if the user wants to convert the BDD to a crossbar
				// Also checks if the original BDD and the created Xbar are equivalent using the truth table comparing function
				if (optional == null)
				{
					System.out.println("Would you like to convert this BDD to a crossbar? y/n");
					String answer = inB.next();
					if (answer.contains("y"))
					{
						System.out.print("Enter save file name (make sure to include file extension): ");
						String saveName = inB.next();
						int[][] convertedBDD = convertBDD(bddArray);
						saveXBAR(saveName, convertedBDD);
						xbarTableResults = printTruthTable('r', convertedBDD, bdd_vars);
					}
					else
						break;

					System.out.println("Checking newly created Xbar and BDD equivalence...");
					compareTruthTables(xbarTableResults, bddTableResults);

				}

			}

			// Xbar Solving
			if (end == 'r')
			{
				// Read the Xbar file and print it to the console
				int[][] xbarArray = readXBAR(file);
				printGraph(xbarArray);

				// Get a save name from the user and save the Xbar as a .txt file
				System.out.print("Enter name for save file (make sure to include file extension): ");
				Scanner inX = new Scanner(System.in);
				String saveName = inX.nextLine();
				saveXBAR(saveName, xbarArray);

				int[] variable_instances = new int[xbar_vars];

				// Retrieve an instance of the Xbar from the user
				for (int i = 0; i < xbar_vars; i++)
				{
					System.out.print("Enter the value for x" + (i+1) + ": ");
					int inputedVar = inX.nextInt();
					if ((inputedVar == 0) || (inputedVar == 1))
					{
						variable_instances[i] = inputedVar;
					}
					else
					{
						System.out.println("Error: Value must be set to 0 or 1. Try again.");
						i--;
					}
				}

				// Create a Boolean array given the input instances and solve the Xbar
				Boolean[][] booleanArray = xbarBoolArray(xbarArray, variable_instances);
				boolean solved = solveXBAR(booleanArray);

				// Print whether or not the Xbar instance can be solved
				if (solved)
				{
					System.out.println("There is a solution to this crossbar instance!");
				}
				else
				{
					System.out.println("There is no solution to this crossbar instance :(");
				}

				// Print the truth table of the input Xbar
				// This involves testing every single possible combination of inputs
				xbarTableResults = printTruthTable(end, xbarArray, xbar_vars);

			}

			// There is not a second file
			if (optional == null)
			{
				secondFile = 2;
			}
			secondFile++;
		}

		// If there is a second file input, then compare their truth table
		if (optional != null)
		{
			compareTruthTables(xbarTableResults, bddTableResults);
		}
	}

	// Takes in a String that is the file name that was passed in the input arguments
	// Returns a 2D int array with the BDD variables
	// Returns a fake array if the file was not found in the directory
	public static int[][] readBDD(String file)
	{
		try
		{
			File input = new File(file);
			Scanner sc = new Scanner(input);

			sc.next(); // Skip string
			bdd_vars = Integer.parseInt(sc.next());
			sc.next(); // Skip string
			int rows = Integer.parseInt(sc.next());

			int[][] bdd = new int[rows][4];
			for (int i = 0; i < rows; i++)
			{
				for (int j = 0; j < 4; j++)
				{
					bdd[i][j] = sc.nextInt();
				}
			}

			return bdd;

		} catch (FileNotFoundException e) {
			System.out.println("Error: The file does not exist in this directory.");
		}

		int[][] fake = new int[1][1];
		return fake;
	}

	// Takes in a String that is the file name that was passed in the input arguments
	// Returns a 2D int array with the Xbar variables
	// Returns a fake array if the file was not found in the directory
	public static int[][] readXBAR(String file)
	{
		try
		{
			File input = new File(file);
			Scanner sc = new Scanner(input);

			sc.next(); // Skip string
			xbar_vars = Integer.parseInt(sc.next());
			sc.next(); // Skip string
			int rows = sc.nextInt();
			sc.next(); // Skip string
			int columns = sc.nextInt();

			int[][] xbar = new int[rows][columns];
			for (int i = 0; i < rows; i++)
			{
				for (int j = 0; j < columns; j++)
				{
					xbar[i][j] = sc.nextInt();
				}
			}

			return xbar;

		} catch (FileNotFoundException e) {
			System.out.println("Error: The file does not exist in this directory.");
		}

		int[][] fake = new int[1][1];
		return fake;
	}

	// Reads the int array that was created in the respective read function and prints it to the console
	public static void printGraph(int[][] array)
	{
		int height = array.length;
		int length = array[0].length;

		// This would only be the case if reading the BDD caused an excpetion
		if (height == 1)
		{
			return;
		}

		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < length; j++)
			{
				System.out.print(array[i][j]);
				System.out.print(" ");
			}
			System.out.println();
		}
	}

	// Takes in a file name that was specified by the user and the 2D int array for the respective graph
	// Saves the Xbar to a .txt file with the name that was inputted by the user
	public static void saveXBAR(String filename, int[][] array)
	{
		try {
			File save = new File(filename);

			if (!save.exists())
			{
				save.createNewFile();
			}

			FileWriter fileWrite = new FileWriter(save.getAbsoluteFile());
            BufferedWriter writer = new BufferedWriter(fileWrite);

			int height = array.length;
			int length = array[0].length;

			// This would only be the case if reading the crossbar caused an excpetion
			if (height == 1)
			{
				return;
			}

			for (int i = 0; i < height; i++)
			{
				for (int j = 0; j < length; j++)
				{
					writer.write(String.valueOf(array[i][j]));
					writer.write(" ");
				}
				writer.write("\n");
			}
			writer.close();

		} catch (IOException e) {
			System.out.println("An error occurred. Try again later.");
		}
	}

	// Prints the truth table for the BDD or the Xbar
	// Computes every single instance of variables of the BDD or Xbar
	public boolean[] printTruthTable(char mode, int[][] array, int variables)
	{
		int num_table_spaces = (int)Math.pow(2,variables);
		boolean[] tableResults = new boolean[num_table_spaces];
		int resultCount = 0;
		boolean solve = false;
		int[] instances = new int[variables];

		for (int i = 0; i < num_table_spaces; i++)
		{

			String binary_rep = Integer.toBinaryString(i);
			int string_size = binary_rep.length();
			int instance_array_location = instances.length - 1;

			// Creates an instance of the truth table
			for (int j = binary_rep.length()-1; j >= 0; j--)
			{
				char value = binary_rep.charAt(j);
				int integer_value = Character.getNumericValue(value);

				instances[instance_array_location] = integer_value;
				instance_array_location--;
			}

			for (int k = 0; k < variables; k++)
			{
				System.out.print(instances[k]);
				System.out.print(" ");
			}

			// Solves if the input file is a BDD
			if (mode == 'd')
			{
				solve = solveBDD(0, array, instances);
			}
			// Solves if the input file is an Xbar
			if (mode == 'r')
			{
				Boolean[][] boolArray = xbarBoolArray(array, instances);
				solve = solveXBAR(boolArray);
			}

			tableResults[resultCount] = solve;
			resultCount++;

			System.out.print(" -> ");
			if (solve)
			{
				System.out.print("f = 1");
			}
			if (!solve)
			{
				System.out.print("f = 0");
			}
			System.out.println();
		}

		return tableResults;
	}

	// Takes the Xbar array and the variable instances and converts the array to a Boolean array
	public Boolean[][] xbarBoolArray(int[][] array, int[] var_assignments)
	{
		int height = array.length;
		int length = array[0].length;
		Boolean[][] boolArray = new Boolean[height][length];

		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < length; j++)
			{
				// Hard coded as 0
				if (array[i][j] == 0)
				{
					boolArray[i][j] = false;
					continue;
				}

				// Hard coded as 1
				if (array[i][j] == 99)
				{
					boolArray[i][j] = true;
					continue;
				}

				// Instance variable
				else
				{

					int sign = 1;
					int current_value = array[i][j];

					if (current_value < 0)
					{
						sign = -1;
						current_value = current_value * -1;
					}

					int assignment = var_assignments[current_value - 1];

					// Flip the assignment because it was negative variable
					if (sign == -1)
					{
						switch (assignment)
						{
							case 0: assignment = 1; break;
							case 1: assignment = 0; break;
						}
					}

					if (assignment == 0)
					{
						boolArray[i][j] = false;
					}

					if (assignment == 1)
					{
						boolArray[i][j] = true;
					}

				}
			}
		}

		return boolArray;
	}

	// Takes the Boolean array that was created in the previous function and solves the Xbar
	public boolean solveXBAR(Boolean[][] array)
	{

		// Base case
		// If the array is of length 1 then we have reached the destination row and the Xbar is solvable
		if(array.length == 1)
			return true;

		// Traverses the row and checks for a 1.
		// If there isn't a 1 in the row then the crossbar isnt solvable in this part of the algorithm
		for (int i = 0; i < array[0].length; i++)
		{
			if(array[0][i])
			{
				for (int j = 1; j < array.length; j++)
				{

					// Makes a new array with the base being the row where a 1 was found
					if (array[j][i])
					{
						Boolean[][] newArray = new Boolean[array.length - j][array[0].length];
						int row = 0;
						for (int x = j; x < array.length; x++)
						{
							for (int y = 0; y < array[0].length; y++)
							{
								newArray[row][y] = array[x][y];
							}
							row++;
						}

						// Pass the new array into the function recursively and try to find a solution
						if (solveXBAR(newArray))
							return true;
						else
							continue;
					}
				}
			}
		}

		return false;
	}

	// Takes in a node ID (starts the algorithm at node 1), the int array, and the respective variable assignments
	public boolean solveBDD(int id, int[][] array, int[] var_assignments)
	{
		// Base case
		// If the second and third numbers are -1 then we have reached a leaf node
		if (array[id][1] == -1 && array[id][2] == -1)
		{
			// Check the value of the leaf and return it
			if (array[id][3] == 1)
				return true;
			else
				return false;
		}

		// If the current node is true, then go to the node located in the second array spot on that row
		if(var_assignments[array[id][3] - 1] == 1)
		{
			return solveBDD(array[id][1] - 1, array, var_assignments);
		}
		// If the current node is false, then go to the node located in the third array spot on that row
		else
		{
			return solveBDD(array[id][2] - 1, array, var_assignments);
		}
	}

	// Takes in the Boolean results of an Xbar truth table and a BDD truth table and compares them
	// Prints if they are equaivalent or not but returns nothing
	public void compareTruthTables(boolean[] xbarTableResults, boolean[] bddTableResults)
	{
		// If the lengths arent exactly the same then they arent equal anyway
		// This just prevents an ArrayOutofBounds Exception
		int compare = Math.min(bddTableResults.length, xbarTableResults.length);
		int equivalent = 0;
		for (int i = 0; i < compare; i++)
		{
			if (bddTableResults[i] != xbarTableResults[i])
			{
				equivalent = 1;
				System.out.println("The two functions are not equivalent!");
				break;
			}
		}

		if (equivalent == 0)
		{
			System.out.println("The two functions are equivalent!");
		}
	}

	// Takes in a BDD int array and creates an Xbar
	public int[][] convertBDD(int[][] bddArray)
	{

		int zeroNodeID = bddArray[bddArray.length - 2][0];
		int oneNodeID = bddArray[bddArray.length - 1][0];

		// Number of nodes determines number of rows
		// Number of edges determine number of columns
		int numNodes = bddArray.length;
		int numEdges = 2*(numNodes - 2);


		// Removes the need to account for edges pointing directly to the 0 node
		for (int i = 0; i < bddArray.length; i++)
		{
			if (bddArray[i][1] == zeroNodeID || bddArray[i][2] == zeroNodeID)
			{
				numEdges--;
			}
		}
		numNodes--;

		System.out.println("Nodes: " + numNodes + " Edges: " + numEdges);

		int[][] converted = new int[numNodes][numEdges];
		int convertedCount = 0;

		// Works up from the nodes closest to the child nodes in the tree
		for (int i = bddArray.length - 3; i >=0; i--)
		{
			for (int j = 1; j <= 2; j++)
			{
				if (bddArray[i][j] != zeroNodeID)
				{
					if (bddArray[i][j] == oneNodeID)
					{
						if (j == 1)
						{
							converted[converted.length-1][convertedCount] = bddArray[i][3];
						}
						else
						{
							converted[converted.length-1][convertedCount] = -bddArray[i][3];
						}

					}
					else
					{
						if (j == 1)
						{
							converted[bddArray[i][j] - 1][convertedCount] = bddArray[i][3];
						}
						else
						{
							converted[bddArray[i][j] - 1][convertedCount] = -bddArray[i][3];
						}

					}

					converted[bddArray[i][0] - 1][convertedCount] = 99;
					convertedCount++;
				}
			}
		}


		return converted;
	}

	public static void main(String args[])
	{
		if (args.length == 1)
		{
			FlowCompute task = new FlowCompute(args[0], null);
		}

		if (args.length == 2)
		{
			FlowCompute task = new FlowCompute(args[0], args[1]);
		}

	}
}
