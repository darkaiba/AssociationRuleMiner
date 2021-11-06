/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package associationruleminer;

import configuracoes.Configuracao;
import estruturas.Regra;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author rafael
 */
public class ExtratorRegras {
    //variaveis para encontrar o maior valor para padronização
    private double confMax = 0.0;
    private double liftMax = 0.0;
    private double riMax = 0.0;
    private double convMax = 0.0;
    private double addVMax = 0.0;
    private double oddRMax = 0.0;
    
    
    public ArrayList<Regra> extrairRegras(HashMap<String,Double> itemsetsFrequentes, Configuracao config){
        
        ArrayList<Regra> regras = new ArrayList<Regra>();
        ArrayList<Regra> rules =  new ArrayList<Regra>();
        
        for(String itemset : itemsetsFrequentes.keySet()){
            String[] partes = itemset.split(",");
            if(partes.length == 1){
                if(config.isExibirCorpoVazio()){
                    double suporte = itemsetsFrequentes.get(partes[0]);
                    Regra regra = new Regra("Ø", partes[0],suporte,suporte,suporte,0,0,0,0,0);
                    regras.add(regra);    
                }
            }else{
                for(int cab=0;cab<partes.length;cab++){
                    String cabeca = partes[cab];
                    String corpo = "";
                    for(int cor=0;cor<partes.length;cor++){
                        if(cab != cor){
                            corpo += partes[cor] + ",";
                        }
                    }
                    corpo = corpo.substring(0, corpo.length()-1);
                    double confianca = calculaConfianca(itemsetsFrequentes,itemset,corpo);
                    if(config.isConfianca()){
                        if(confianca < config.getConfMin()){
                            continue;
                        }
                    }
                    if(confMax < confianca){
                        confMax = confianca;
                    }
                    
                    double lift = calculaLift(itemsetsFrequentes,itemset,corpo,cabeca);
                    if(config.isLift()){
                        if(lift < config.getLiftMin()){
                            continue;
                        }
                    }
                    if(liftMax < lift){
                        liftMax = lift;
                    }
                    
                    double ri = calculaRI(itemsetsFrequentes,itemset,corpo,cabeca);
                    if(config.isRi()){
                        if(ri < config.getRiMin()){
                            continue;
                        }
                    }
                    if(riMax < ri){
                        riMax = ri;
                    }
                    
                    double conviccao = calculaConviccao(itemsetsFrequentes,itemset,corpo,cabeca);
                    if(config.isConviccao()){
                        if(conviccao < config.getConviccaoMin()){
                            continue;
                        }
                    }
                    if(convMax < conviccao){
                        convMax = conviccao;
                    }
                    
                    double addedValue = calculaAddedValue(itemsetsFrequentes,itemset,corpo,cabeca);
                    if(config.isAddedValue()){
                        if(addedValue < config.getAddedValueMin()){
                            continue;
                        }
                    }
                    if(addVMax < addedValue){
                        addVMax = addedValue;
                    }
                    
                    double oddsRatio = calculaOddsRatio(itemsetsFrequentes,itemset,corpo,cabeca);
                    if(config.isOddsRatio()){
                        if(oddsRatio < config.getOddsRatioMin()){
                            continue;
                        }
                    }
                    if(oddRMax< oddsRatio){
                        oddRMax = oddsRatio;
                    }                   
                                        
                    Regra regra = new Regra(corpo, cabeca, itemsetsFrequentes.get(itemset), confianca, lift, ri, conviccao, addedValue, oddsRatio, 0);
                    regras.add(regra);
                }
            }
        }
        
        //laço para atualizar o score
        for(int i=0;i<regras.size();i++){
            
            //inserção da nova variável score e atribuição dos pontos
            double score = calculaScore(regras.get(i).getConfianca(), regras.get(i).getLift(), regras.get(i).getRi(), regras.get(i).getConviccao(), regras.get(i).getAddedValue(), regras.get(i).getOddsRatio());
            if(config.isScore()){
                if(score < config.getScoreMin()){
                    continue;
                }
            }
            
            //atualização do score
            regras.get(i).setEmsemble(score);
            rules.add(regras.get(i));
        }
        //alterou a variavel de retorno
        return rules;
    }
    
    private double calculaConfianca(HashMap<String,Double> itemsetsFrequentes, String itemset, String corpo){
        double supItemset = itemsetsFrequentes.get(itemset);
        if(!itemsetsFrequentes.containsKey(corpo)){
            System.out.println("Treta!");
        }
        if(!itemsetsFrequentes.containsKey(corpo)){
            System.out.println("Aqui!!!!");
        }
        double supCorpo = itemsetsFrequentes.get(corpo);
        
        if(supCorpo == 0){
            return 0;
        }else{
            return supItemset / supCorpo;
        }
    }
    
    private double calculaLift(HashMap<String,Double> itemsetsFrequentes, String itemset, String corpo, String cabeca){
        double supItemset = itemsetsFrequentes.get(itemset);
        double supCorpo = itemsetsFrequentes.get(corpo);
        double supCabeca = itemsetsFrequentes.get(cabeca);
        
        if(supCorpo == 0 || supCabeca == 0){
            return 0;
        }else{
            return supItemset / (supCorpo * supCabeca);
        }
        
        
    }
    
    private double calculaRI(HashMap<String,Double> itemsetsFrequentes, String itemset, String corpo, String cabeca){
        double supItemset = itemsetsFrequentes.get(itemset);
        double supCorpo = itemsetsFrequentes.get(corpo);
        double supCabeca = itemsetsFrequentes.get(cabeca);
        
        if(supCorpo == 0 || supCabeca == 0){
            return 0;
        }else{
            return supItemset - (supCorpo * supCabeca);
        }
        
    }
    
    private double calculaConviccao(HashMap<String,Double> itemsetsFrequentes, String itemset, String corpo, String cabeca){
        double numerador1 = itemsetsFrequentes.get(corpo);
        double numerador2 = 1 - itemsetsFrequentes.get(cabeca);
        double denominador = itemsetsFrequentes.get(corpo) - itemsetsFrequentes.get(itemset);
        
        if(denominador == 0){
            return 0;
        }else{
            return (numerador1 * numerador2) / denominador;
        }
        
    }
    
    private double calculaAddedValue(HashMap<String,Double> itemsetsFrequentes, String itemset, String corpo, String cabeca){
        double supItemset = itemsetsFrequentes.get(itemset);
        double supCorpo = itemsetsFrequentes.get(corpo);
        double supCabeca = itemsetsFrequentes.get(cabeca);
        
        if(supCorpo == 0){
            return 0;
        }else{
            return ((supItemset / supCorpo) - supCabeca);
        }
    }
    
    private double calculaOddsRatio(HashMap<String,Double> itemsetsFrequentes, String itemset, String corpo, String cabeca){
        double pA = itemsetsFrequentes.get(corpo);
        double pB = itemsetsFrequentes.get(cabeca);
        double pAB = itemsetsFrequentes.get(itemset);
        double pNotANotB = 1 - ((pA + pB) - pAB);
        double pANotB = pAB - pB;
        double pNotAB = pAB - pA;
        
        
        double numerador = pAB * pNotANotB;
        double denominador = pANotB * pNotAB;
        
        if(denominador == 0){
            return 0;
        }else{
            return (numerador / denominador);
        }
    }
    //implentanção da função que calcula o score, e padroniza os dados
    private double calculaScore(double confianca, double lift, double rules, double conviccao, double addedValue, double oddsRatio){
        double conf = confianca/confMax;
        double lf = lift/liftMax;
        double ri = rules/riMax;
        double conv = conviccao/convMax;
        double addV = addedValue/addVMax;
        double oddR = oddsRatio/oddRMax;
        //pontuaçãofica entre 0 e 10
        return ((conf+lf+ri+conv+addV+oddR)/6)*10;
    }
   
}
