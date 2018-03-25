package com.elasticcloudservice.predict;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Predict {
	private static final double ALPHA = 0.0001;
	private static final int ITERATION = 300000;
	private  static  int KEY = 20;

	public static String[] predictVm(String[] ecsContent, String[] inputContent, int KEY) throws IOException {
		Predict.KEY=KEY;
		final int SUM_KINDS_OF_FLAVORS = 15;
		/**
		 * 获得56 128 1200
		 */
		String physicalServer = inputContent[0];
		/**
		 * 获得3
		 */
		int typeOfECS = Integer.parseInt(inputContent[2]);

		/**
		 * 获得每一个型号的flavor情况
		 */
		String[] inputFlavors = new String[typeOfECS];
		for (int i=3;i<typeOfECS+3;i++) {
			inputFlavors[i-3]=inputContent[i];
		}

		/**
		 * 优化的目标，开始日期与结束日期
		 */
		String paramToOptimize = inputContent[inputContent.length-4];
		String beginDate=inputContent[inputContent.length-2];
		String endDate=inputContent[inputContent.length-1];

		/**
		 * 获得预测结果
		 */
		List<int[]> resultOfDaysOfFlavors = 							//get flavor of days,List.get(i)[j]=
				predictAll(ecsContent,beginDate,endDate,inputFlavors);	//number of flavori at day j,j=0 shows the sum.

		ArrayList<Integer> resultOfNumbersOfFlavors =					//get sum of flavors and number of every flavor,
				new ArrayList<>(SUM_KINDS_OF_FLAVORS+1);		//the first item show the sum,list.get(i) shows flavori.

		resultOfNumbersOfFlavors.add(0);								//first item to count

		int[] numberOfFlavors = DataUtil.getNumbersOfFlavors(inputFlavors);		//get the flavor number to be predicted
		/** ===================================
		 * get number of flavors
		 * if the flavor is not to be predicted,number=-1
		 * */
		for (int i=1;i<SUM_KINDS_OF_FLAVORS+1;i++) {
			if (DataUtil.numberInArray(i, numberOfFlavors)) {
				resultOfNumbersOfFlavors.add(resultOfDaysOfFlavors.get(i)[0]);
				resultOfNumbersOfFlavors.set(0, resultOfNumbersOfFlavors.get(0) + resultOfNumbersOfFlavors.get(i));
			} else {
				resultOfNumbersOfFlavors.add(-1);
			}
		}

		/**
		 * 解析第一行的数据
		 */
		String[] physicalServerPara = physicalServer.split(" ");	//get param of physical server
		int serverTypeCPU = Integer.parseInt(physicalServerPara[0]);	//get number of CPU of physical server
		int serverTypeMemory = Integer.parseInt(physicalServerPara[1]);	//get number of memory of physical server
		/** ==================================
		 * get result of boxing
		 * **/
		String[] results =
				PutServer.putservermethod(serverTypeCPU,serverTypeMemory,resultOfNumbersOfFlavors,paramToOptimize);
		return results;
	}

	/**
	 * predict one flavor
	 * param trainDataStringArray:train data in format of String array
	 * param falvor:the number of flavor to be predicted
	 * param days:how many days need to be predicted
	 * return int[]:the first item store the sum,int[i] store dayi**/
	public static int[] predictOneFlavor(String[] trainDataStringArray, int flavor, int days) throws IOException {
		/**
		 * 获得每一条训练数据
		 */
		List<double[]> dataList =
				DataUtil.loadDataFromStringArray(trainDataStringArray);			//get data list

		/**
		 * 获得针对某一个型号的flavor的数据
		 */
		double[][] AllData =
				DataUtil.getFlavorArrayFromDataList(flavor, KEY, dataList);

		/**
		 * 从这里开始调用神经网络
		 */
		//对输入输出做处理
		int AllDataRow = AllData.length;
		int AllDataCol = AllData[0].length;
		//分离数据为训练
		double[][] trainData = new double[AllDataRow][AllDataCol-1];
		for(int i=0;i<AllDataRow;i++){
			for(int j=0;j<AllDataCol-1;j++){
				trainData[i][j] = AllData[i][j];
			}
		}

		//分离数据为输出
		double[][] trainRes = new double[AllDataRow][1];
		for(int i=0;i<AllDataRow;i++){
			trainRes[i][0] = AllData[i][AllDataCol-1];
		}

		//初始化神经网络的基本配置
		//第一个参数是一个整型数组，表示神经网络的层数和每层节点数，比如{3,10,10,10,10,2}表示输入层是3个节点，输出层是2个节点，中间有4层隐含层，每层10个节点
		//第二个参数是学习步长，第三个参数是动量系数
		BpDeep bp = new BpDeep(new int[]{10,10,1}, 0.02, 0.8);

		//进行训练，1000为迭代训练次数
		for(int n=0;n<5000;n++)
			for(int i=0;i<AllDataRow;i++){
				//System.out.println("->"+Arrays.toString(trainData[i])+" "+Arrays.toString(trainRes[i]));
				bp.train(trainData[i], trainRes[i]);
			}


		double[] newTrainData = new double[KEY+1];
		double[][] newTrainRes = new double[days + 1][1];
		int[] result_int = new int[days+1];

		System.arraycopy(AllData[AllDataRow - 1], 2, newTrainData, 1, KEY);
		newTrainData[0] = 1.0;

		for(int i=1;i<=days;i++){
			//System.out.println("-->"+Arrays.toString((newTrainData)));
			newTrainRes[i] = bp.computeOut(newTrainData);
			if (newTrainRes[i][0] < 0) {
				result_int[i] = 0;
			} else result_int[i] =  (int)Math.round(newTrainRes[i][0]);

			double[] temp = newTrainData;
			System.arraycopy(temp, 2, newTrainData, 1, KEY - 1);
			newTrainData[KEY] = newTrainRes[i][0];
		}

		for (int r : result_int) {
			result_int[0]=result_int[0]+r;
		}
		//System.out.println(Arrays.toString(result_int));
		return result_int;
		/**
		 * 线性规划，先注视掉这个方法
		 */
/*		LinearRegression m = new LinearRegression(trainData, ALPHA, ITERATION);
		m.trainTheta();

		double[] history = new double[KEY + 1];
		double[] temp = new double[KEY + 1];
		double[] result_double = new double[days+1];
		int[] result_int = new int[days+1];




		System.arraycopy(trainData[trainData.length - 1], 2, history, 1, KEY);
		history[0] = 1.0;

		for (int i = 1; i < days+1; i++) {
			for (int j = 0; j < KEY + 1; j++) {
				result_double[i] = result_double[i] + m.getTheta()[j] * history[j];
			}
			if (result_double[i] < 0) {
				result_int[i] = 0;
			} else result_int[i] = (int) Math.round(result_double[i]);
			temp = history;
			System.arraycopy(temp, 2, history, 1, KEY - 1);
			history[KEY] = result_double[i];
		}
		for (int r : result_int) {
			result_int[0]=result_int[0]+r;
		}
		System.out.println(Arrays.toString(result_int));
		return result_int;*/
	}

	/**
	 * predict all kinds of flavors
	 * param trainDataStringArray:train data in format of String array
	 * param beginDate:the begin date of prediction
	 * param endDate:the end date of prediction
	 * param inputFlavors:the flavors need to be predicted
	 * return:List<int[]>:**/
	public static List<int[]> predictAll(String[] trainDataStringArray,String beginDate, String endDate, String[] inputFlavors) throws IOException {
		/**
		 * 计算日期间隔
		 */
		int days = DataUtil.calDaysBetween(beginDate, endDate);					//calculate days
		int[] result;															//create temp array for predictOneFlavor
		List<int[]> resultList = new ArrayList<>();								//create return list
		for (int i=0;i<16;i++){
			resultList.add(new int[days+1]);
		}
		for (String inputFlavor:inputFlavors) {
			/**
			 * 获得每一个的型号，然后某一个型号开始预测，最后将结果加入进去
			 */
			int numberOfFlavor = DataUtil.getNumberOfFlavor(inputFlavor);
			result = predictOneFlavor(trainDataStringArray,numberOfFlavor,days);
			resultList.set(numberOfFlavor,result);
		}
		return resultList;
	}


}

