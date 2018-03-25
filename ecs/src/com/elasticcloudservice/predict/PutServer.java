package com.elasticcloudservice.predict;


import java.util.ArrayList;

public class PutServer {
    public static int judgeCPU(String s) {//·????é???úCPU??????
        if(s.equals("flavor1")||s.equals("flavor2")||s.equals("flavor3"))
            return 1;
        else if(s.equals("flavor4")||s.equals("flavor5")||s.equals("flavor6"))
            return 2;
        else if(s.equals("flavor7")||s.equals("flavor8")||s.equals("flavor9"))
            return 4;
        else if(s.equals("flavor10")||s.equals("flavor11")||s.equals("flavor12"))
            return 8;
        else if(s.equals("flavor13")||s.equals("flavor14")||s.equals("flavor15"))
            return 16;
        else
            return 0;
    }
    public static int judgeMemory(String s) {//·????é???úMemory??????
        if(s.equals("flavor1"))
            return 1;
        else if(s.equals("flavor2")||s.equals("flavor4"))
            return 2;
        else if(s.equals("flavor3")||s.equals("flavor5"))
            return 4;
        else if(s.equals("flavor7")||s.equals("flavor8")||s.equals("flavor10")||s.equals("flavor6"))
            return 8;
        else if(s.equals("flavor9")||s.equals("flavor11")||s.equals("flavor13"))
            return 16;
        else if(s.equals("flavor12")||s.equals("flavor14"))
            return 32;
        else if(s.equals("flavor15"))
            return 64;
        else
            return 0;
    }
    public static int remainVirtual(ArrayList<Integer> input) {//·????é???ú??±í???à?????é???ú????
        return input.get(0);
    }
    public static int maxVirtualIndex(ArrayList<Integer> input) {//·???????????±ê×??ó???é???ú???????????????é???ú????·???0
        for(int i=15;i>=1;i--)
            if(input.get(i)!=0&&input.get(i)!=-1)
                return i;
        return 0;
    }
    public static void first_fit_putVirtualIntoServer(int ServerTypeCPU,int ServerTypeMemory,ArrayList<Server> ServerList,String Virtual){
        for(int i=0;i<ServerList.size();i++)//????????·????÷????±é?ú
            if(ServerList.get(i).RemainCPU>=judgeCPU(Virtual)&&ServerList.get(i).RemainMemory>=judgeMemory(Virtual)){//??????i??·????÷??·??????é???ú
                ServerList.get(i).RemainCPU=ServerList.get(i).RemainCPU-judgeCPU(Virtual);
                ServerList.get(i).RemainMemory=ServerList.get(i).RemainMemory-judgeMemory(Virtual);
                int num=ServerList.get(i).VirtualList.get(Virtual);
                ServerList.get(i).VirtualList.put(Virtual, num+1);
                return;
            }
        ServerList.add(new Server(ServerTypeCPU,ServerTypeMemory));//????·?????????????????·????÷????????????·????÷??????·???
        ServerList.get(ServerList.size()-1).RemainCPU=ServerList.get(ServerList.size()-1).RemainCPU-judgeCPU(Virtual);
        ServerList.get(ServerList.size()-1).RemainMemory=ServerList.get(ServerList.size()-1).RemainMemory-judgeMemory(Virtual);
        int num=ServerList.get(ServerList.size()-1).VirtualList.get(Virtual);
        ServerList.get(ServerList.size()-1).VirtualList.put(Virtual, num+1);
    }

    public static String[] putservermethod(int ServerTypeCPU,int ServerTypeMemory,ArrayList<Integer> VirtualList_int,String judgeType) {//·???·?·???????????·?±????è???????????????????·??·????÷CPU??·????÷???????é???ú??±í
        //?é???ú??±í??±ê0???é???ú×???????±ê1-15·?±????é???ú1??15??????????±ê??-1±í?????è?????????é???ú????±ê?ó??????0???è?????????é???ú
        ArrayList<Integer> VirtualList_int_copy=new ArrayList<Integer>();
        VirtualList_int_copy.addAll(VirtualList_int);
        ArrayList<Server> ServerList=new ArrayList<Server>();//??????·????÷×???·???
        ServerList.add(new Server(ServerTypeCPU,ServerTypeMemory));

        /**
         * 首次适应算法
         */
        exchange_first_fit_putVirtualIntoServer(ServerTypeCPU,ServerTypeMemory,ServerList,VirtualList_int,judgeType);

        Integer ServerCount=ServerList.size();//·????÷??×???
        //?????á????????????????????????????????
		/*
                        ?€?????é???ú×???
		        ?é???ú????????1 ?é???ú????
		        ?é???ú????????2 ?é???ú????
		   ????????
                        ?ù?è???í·????÷×???
                                ???í·????÷1 ?é???ú????????1 ??·??????à???é???ú???? ?é???ú????????2 ??·??????à???é???ú???? ????
                                ???í·????÷2 ?é???ú????????1 ??·??????à???é???ú???? ?é???ú????????2 ??·??????à???é???ú???? ????
           ????????
        */
        ArrayList<String> FinalResult=new ArrayList<String>();
        FinalResult.add(VirtualList_int_copy.get(0).toString()+"\r");
        for(int i=1;i<=15;i++)
            if(VirtualList_int_copy.get(i)!=-1)//???????è?????????é???ú????????????0????????
                FinalResult.add("flavor"+i+" "+VirtualList_int_copy.get(i)+"\r");
        FinalResult.add("\r");
        FinalResult.add(ServerCount.toString()+"\r");
        for(int i=0;i<ServerList.size();i++){
            String TempString=""+(i+1)+" ";
            int j=1;
            while(j<16){
                if(ServerList.get(i).VirtualList.get("flavor"+j)>0){
                    TempString=TempString+"flavor"+j+" "+ServerList.get(i).VirtualList.get("flavor"+j)+" ";
                    j++;
                }
                else
                    j++;
            }
            TempString=TempString+"\r";
            FinalResult.add(TempString);
        }
        String[] FinalResultshuzu=FinalResult.toArray(new String[0]);
        return FinalResultshuzu;
    }

    public static int orderMaxVirtualIndex(ArrayList<Integer> VirtualList,int[] Order){
        for(int i=14;i>=0;i--)
            if(VirtualList.get(Order[i])>0)
                return Order[i];
        return 0;
    }

    public static int findMinVirtual(String JudgeResource,Server CurrentServer,int CurrentFlavor){//·???????·????÷??????×???×??????í????×????à?????é???ú
        if(JudgeResource.equals("CPU")){//??????????????CPU????????????CurrentSrever??Memory??CurrentFlavor?à????????CPU×?????????flavor??±à??
            for(int i=1;i<=15;i++){
                if(CurrentServer.VirtualList.containsKey("flavor"+i)&&CurrentServer.VirtualList.get("flavor"+i)>0&&judgeCPU("flavor"+i)<judgeCPU("flavor"+CurrentFlavor)&&judgeMemory("flavor"+i)==judgeMemory("flavor"+CurrentFlavor)&&CurrentServer.RemainCPU+judgeCPU("flavor"+i)-judgeCPU("flavor"+CurrentFlavor)>=0)
                    //??·?????1????flavor??????2????flavor??Memory??currentflavor?à????3????Flavor??CPU±?currentflavor??????4??·????÷?????????í????
                    return i;
            }
            return 0;//·???0±í????????????????
        }
        else{//????????Memory
            int[] Order=new int[]{1,2,4,3,5,6,7,8,10,9,11,13,12,14,15};
            for(int i=0;i<=14;i++){
                if(CurrentServer.VirtualList.containsKey("flavor"+i)&&CurrentServer.VirtualList.get("flavor"+i)>0&&judgeMemory("flavor"+Order[i])<judgeMemory("flavor"+CurrentFlavor)&&judgeCPU("flavor"+Order[i])==judgeCPU("flavor"+CurrentFlavor)&&CurrentServer.RemainMemory+judgeMemory("flavor"+Order[i])-judgeMemory("flavor"+CurrentFlavor)>=0)
                    return Order[i];
            }
            return 0;//·???0±í????????????????
        }
    }

    public static void exchange_first_fit_putVirtualIntoServer(int ServerTypeCPU,int ServerTypeMemory,ArrayList<Server> ServerList/*????????????????????????·????÷*/,ArrayList<Integer> VirtualList,String JudgeType){
        int[] Order=new int[15];
        if(JudgeType.equals("CPU")){//°???CPU?ó???????ò
            for(int i=0;i<15;i++)
                Order[i]=i+1;
        }
        if(JudgeType.equals("MEM")){//°???Memory?ó???????ò
            Order[0]=1;Order[1]=2;Order[2]=4;Order[3]=3;Order[4]=5;Order[5]=6;Order[6]=7;Order[7]=8;Order[8]=10;Order[9]=9;
            Order[10]=11;Order[11]=13;Order[12]=12;Order[13]=14;Order[14]=15;
        }
        while(remainVirtual(VirtualList)!=0){//????Virtual??????
            int StartIndex=orderMaxVirtualIndex(VirtualList,Order);
            for(int i=0;i<ServerList.size()+1;i++){//????????·????÷????±é?ú
                if(i!=ServerList.size()){
                    if(ServerList.get(i).RemainCPU>=judgeCPU("flavor"+StartIndex)&&ServerList.get(i).RemainMemory>=judgeMemory("flavor"+StartIndex)){//????????×???????×°??
                        ServerList.get(i).RemainCPU=ServerList.get(i).RemainCPU-judgeCPU("flavor"+StartIndex);
                        ServerList.get(i).RemainMemory=ServerList.get(i).RemainMemory-judgeMemory("flavor"+StartIndex);
                        ServerList.get(i).VirtualList.put("flavor"+StartIndex, ServerList.get(i).VirtualList.get("flavor"+StartIndex)+1);
                        VirtualList.set(0, VirtualList.get(0)-1);
                        VirtualList.set(StartIndex, VirtualList.get(StartIndex)-1);
                        break;
                    }
                    else if(ServerList.get(i).RemainCPU<judgeCPU("flavor"+StartIndex)&&ServerList.get(i).RemainMemory>=judgeMemory("flavor"+StartIndex)){//CPU·???????????Memory·?????
                        int MaybeExchangeIndex=findMinVirtual("CPU", ServerList.get(i), StartIndex);
                        if(MaybeExchangeIndex==0)
                            continue;
                        else{
                            VirtualList.set(MaybeExchangeIndex, VirtualList.get(MaybeExchangeIndex)+1);
                            VirtualList.set(StartIndex, VirtualList.get(StartIndex)-1);
                            ServerList.get(i).RemainCPU=ServerList.get(i).RemainCPU+judgeCPU("flavor"+MaybeExchangeIndex)-judgeCPU("flavor"+StartIndex);
                            ServerList.get(i).VirtualList.put("flavor"+MaybeExchangeIndex,ServerList.get(i).VirtualList.get("flavor"+MaybeExchangeIndex)-1);
                            ServerList.get(i).VirtualList.put("flavor"+StartIndex,ServerList.get(i).VirtualList.get("flavor"+StartIndex)+1);
                            break;
                        }
                    }
                    else if(ServerList.get(i).RemainCPU>judgeCPU("flavor"+StartIndex)&&ServerList.get(i).RemainMemory<=judgeMemory("flavor"+StartIndex)){//Memory·?????
                        int MaybeExchangeIndex=findMinVirtual("MEM", ServerList.get(i), StartIndex);
                        if(MaybeExchangeIndex==0)
                            continue;
                        else{
                            VirtualList.set(MaybeExchangeIndex, VirtualList.get(MaybeExchangeIndex)+1);
                            VirtualList.set(StartIndex, VirtualList.get(StartIndex)-1);
                            ServerList.get(i).RemainMemory=ServerList.get(i).RemainMemory+judgeMemory("flavor"+MaybeExchangeIndex)-judgeMemory("flavor"+StartIndex);
                            ServerList.get(i).VirtualList.put("flavor"+MaybeExchangeIndex,ServerList.get(i).VirtualList.get("flavor"+MaybeExchangeIndex)-1);
                            ServerList.get(i).VirtualList.put("flavor"+StartIndex,ServerList.get(i).VirtualList.get("flavor"+StartIndex)+1);
                            break;
                        }
                    }
                }
                else{
                    ServerList.add(new Server(ServerTypeCPU,ServerTypeMemory));//????·?????????????????·????÷????????????·????÷??????·???
                    ServerList.get(ServerList.size()-1).RemainCPU=ServerTypeCPU-judgeCPU("flavor"+StartIndex);
                    ServerList.get(ServerList.size()-1).RemainMemory=ServerTypeMemory-judgeMemory("flavor"+StartIndex);
                    ServerList.get(ServerList.size()-1).VirtualList.put("flavor"+StartIndex, 1);
                    VirtualList.set(0, VirtualList.get(0)-1);
                    VirtualList.set(StartIndex, VirtualList.get(StartIndex)-1);
                    break;
                }
            }
            //??????????×°????
        }
    }

}

