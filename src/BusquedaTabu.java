
import java.util.ArrayList;

public class BusquedaTabu {
    private Coordenada[] coordenadas;
    private ArrayList<Integer> solucionInicial;
    private ArrayList<Movimiento> listaTabu;
    private ArrayList<Integer> solucionActual;
    private int costeSolucionActual;
    private int[][] distancias;
    public BusquedaTabu(Coordenada[] coordenadas, ArrayList<Integer> solucionInicial) {
        this.coordenadas = coordenadas;
        this.solucionInicial = solucionInicial;
        this.listaTabu = new ArrayList<>();
        this.solucionActual = solucionInicial;
        this.calcularDistancias();
        this.costeSolucionActual = this.calcularCosteSolucionActual(this.solucionInicial);
    }

    public BusquedaTabu(Coordenada[] coordenadas) {
        this.coordenadas = coordenadas;
        listaTabu = new ArrayList<>();
        this.solucionInicial = new ArrayList<>();
        //Generar solucion inicial greedy cogiendo la siguiente ciudad que esté más cerca
        int indexCoordenadaMin = 0;
        int distanciaMin = Integer.MAX_VALUE;
        int distanciaActual;
        int ciudadActual = 0;
        ArrayList<Integer> ciudadesRecorridas = new ArrayList<>();

        ciudadesRecorridas.add(0);
        for(int k = 1; k < coordenadas.length; k++){
            for(int i = 0; i < coordenadas.length; i++){
                if(!ciudadesRecorridas.contains(i)){
                    distanciaActual = (int)this.coordenadas[ciudadActual].distancia(this.coordenadas[i]);
                    /*if(k == 33){
                        System.out.println("Distancia "+ciudadActual+" a " +i+": "+distanciaActual);
                    }*/
                    if(distanciaActual < distanciaMin){
                        distanciaMin = distanciaActual;
                        indexCoordenadaMin = i;
                    }
                }
            }
            solucionInicial.add(indexCoordenadaMin);
            ciudadesRecorridas.add(indexCoordenadaMin);
            distanciaMin = Integer.MAX_VALUE;
            ciudadActual = indexCoordenadaMin;
        }

        solucionActual = solucionInicial;
        this.calcularDistancias();
        this.costeSolucionActual = this.calcularCosteSolucionActual(this.solucionActual);
    }

    public void calcularDistancias(){
        this.distancias = new int[coordenadas.length][coordenadas.length];

        for(int i = 0; i < coordenadas.length; i++){
            for(int j = 0; j < coordenadas.length; j++){
                this.distancias[i][j] = (int)coordenadas[i].distancia(coordenadas[j]);
            }
        }
    }

    public void solucion() {
        int iteraciones = 10000;
        int reinicio = 0;
        int noMejora = 0;
        int iteracionMejor = 0;
        ArrayList<Integer> vecinoActual;
        ArrayList<Integer> mejorVecino;
        ArrayList<Integer> ultimoMejor = new ArrayList<>();
        int costeMejorVecino;
        int costeVecinoActual;
        Movimiento mejorIntercambio;
        int j, k;
        boolean mejora = false;

        this.imprimirSolucionInicial();
        for (int iter = 1; iter <= iteraciones; iter++) {
            costeMejorVecino = 0;
            mejorIntercambio = new Movimiento();
            mejorVecino = new ArrayList<>();
            vecinoActual = new ArrayList<>();
            j = 0;
            k = 0;
            //generamos los vecinos
            while (j < coordenadas.length - 1) {
                while (j > k) {
                    Movimiento mov = new Movimiento(j, k);
                    //Si está en la lista tabú seguimos iterando
                    if (listaTabu.contains(mov)) {
                        ++k;
                        continue;
                    }
                    if (!mejora && !ultimoMejor.isEmpty()) {
                        int temp2 = ultimoMejor.get(j);
                        vecinoActual.clear();
                        vecinoActual.addAll(ultimoMejor);
                        vecinoActual.set(j, ultimoMejor.get(k));
                        vecinoActual.set(k, temp2);
                        costeVecinoActual = this.calcularCosteSolucionActual(vecinoActual);
                    } else {
                        //Si no está creamos el nuevo vecino intercambiando las posiciones
                        int temp = this.solucionActual.get(j);
                        vecinoActual.clear();
                        vecinoActual.addAll(this.solucionActual);
                        vecinoActual.set(j, this.solucionActual.get(k));
                        vecinoActual.set(k, temp);
                        costeVecinoActual = this.calcularCosteSolucionActual(vecinoActual);
                    }
                    //Si el mejorVecino aun no se encontro, es la primera vez que se explora se crea y se inicializan
                    //todos los parametros necesarios
                    if (mejorVecino.isEmpty()) {
                        mejorVecino.addAll(vecinoActual);
                        costeMejorVecino = costeVecinoActual;
                        mejorIntercambio.setIndexi(mov.getIndexi());
                        mejorIntercambio.setIndexj(mov.getIndexj());
                    }
                    //Si el mejorVecino ya existe se compara el coste de cada solucion
                    else {
                        //Si el coste del vecino actual es mejor se cambia el mejorVecino
                        if (costeVecinoActual < costeMejorVecino) {
                            mejorVecino.clear();
                            mejorVecino.addAll(vecinoActual);
                            costeMejorVecino = costeVecinoActual;
                            mejorIntercambio.setIndexi(mov.getIndexi());
                            mejorIntercambio.setIndexj(mov.getIndexj());
                        }
                    }
                    ++k;
                }
                k = 0;
                ++j;
            }

            ultimoMejor.clear();
            ultimoMejor.addAll(mejorVecino);
            insertarEnTabu(mejorIntercambio);
            if (costeMejorVecino < costeSolucionActual) {
                solucionActual.clear();
                solucionActual.addAll(mejorVecino);
                costeSolucionActual = costeMejorVecino;
                iteracionMejor = iter;
                mejora = true;
                noMejora = 0;
            } else {
                mejora = false;
                ++noMejora;
            }
            this.imprimirSolucionActual(mejorVecino, iter, costeMejorVecino, noMejora, mejorIntercambio);
            if (noMejora == 100) {
                reinicio++;
                System.out.println("\n***************");
                System.out.println("REINICIO: " + reinicio);
                System.out.println("***************");
                listaTabu.clear();
                noMejora = 0;
                ultimoMejor.clear();
                mejora = true;
            }
        }

        //Imprimimos mejor solucion
        System.out.println("\nMEJOR SOLUCION:");
        System.out.print("\tRECORRIDO: ");

        for (int i = 0; i < solucionActual.size(); i++) {
            System.out.print(solucionActual.get(i) + " ");
        }

        System.out.println("\n\tCOSTE (km): " + this.costeSolucionActual);
        System.out.println("\tITERACION: " + iteracionMejor);
    }

    private void insertarEnTabu(Movimiento mov) {
        if (this.listaTabu.size() == 100) {
            this.listaTabu.remove(0);
        }
        this.listaTabu.add(mov);
    }

    private int calcularCosteSolucionActual(ArrayList<Integer> solucion) {
        int coste = this.distancias[0][solucion.get(0)];

        for (int i = 0; i < (solucion.size() - 1); i++) {
            coste += (int) distancias[solucion.get(i)][solucion.get(i + 1)];
        }
        coste += distancias[solucion.get(solucion.size() - 1)][0];
        return coste;
    }

    private void imprimirSolucionActual(ArrayList<Integer> solucionActual,int iteracion, int coste, int noMejora, Movimiento mov) {
        System.out.println("\nITERACION: " + iteracion);
        System.out.println("\tINTERCAMBIO: (" + mov.getIndexi() + ", " + mov.getIndexj() +")");
        System.out.print("\tRECORRIDO: ");

        for (int i = 0; i < solucionActual.size(); i++) {
            System.out.print(solucionActual.get(i) + " ");
        }

        System.out.println("\n\tCOSTE (km): " + coste);
        System.out.println("\tITERACIONES SIN MEJORA: "+ noMejora);
        System.out.println("\tLISTA TABU:");

        for (int i = 0; i < listaTabu.size(); i++) {
            System.out.println("\t" + listaTabu.get(i).getIndexi() + " " + listaTabu.get(i).getIndexj());
        }
    }

    private void imprimirSolucionInicial() {
        System.out.println("RECORRIDO INICIAL");
        System.out.print("\tRECORRIDO: ");
        for (int i = 0; i < this.solucionInicial.size(); i++) {
            System.out.print(this.solucionInicial.get(i) + " ");
        }
        System.out.println("\n\tCOSTE (km): " + this.costeSolucionActual);
    }
}
