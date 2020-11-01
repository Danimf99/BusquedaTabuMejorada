
import java.security.acl.AclNotFoundException;
import java.util.ArrayList;

public class BusquedaTabu {
    private Coordenada[] coordenadas;
    private ArrayList<Integer> solucionInicial;
    private ArrayList<Integer> solucionActual;
    private ArrayList<Integer> mejorSolucion;
    private ArrayList<Movimiento> listaTabu;
    private int costeSolucionActual;
    private int[][] distancias;
    private int[][] frecuencias;
    private int[][] nuevasDistancias;
    private int dMax;
    private int dMin;

    public BusquedaTabu(Coordenada[] coordenadas, ArrayList<Integer> solucionInicial) {
        this.coordenadas = coordenadas;
        this.solucionInicial = solucionInicial;
        this.solucionActual = solucionInicial;
        this.listaTabu = new ArrayList<>();
        this.distancias = new int[coordenadas.length][coordenadas.length];
        this.frecuencias = new int[coordenadas.length][coordenadas.length];
        this.nuevasDistancias = new int[coordenadas.length][coordenadas.length];
        this.mejorSolucion = new ArrayList<>();
        this.dMax = 0;
        this.dMin = Integer.MAX_VALUE;
        this.calcularDistancias();
        this.costeSolucionActual = this.calcularCosteSolucionActual(this.solucionInicial);
    }

    public BusquedaTabu(Coordenada[] coordenadas) {
        this.dMax = 0;
        this.dMin = Integer.MAX_VALUE;
        this.coordenadas = coordenadas;
        this.listaTabu = new ArrayList<>();
        this.solucionInicial = new ArrayList<>();
        this.mejorSolucion = new ArrayList<>();
        this.distancias = new int[coordenadas.length][coordenadas.length];
        this.frecuencias = new int[coordenadas.length][coordenadas.length];
        this.nuevasDistancias = new int[coordenadas.length][coordenadas.length];
        this.calcularDistancias();
        this.calcularSolucionVorazInicial();
        solucionActual = solucionInicial;
        this.costeSolucionActual = this.calcularCosteSolucionActual(this.solucionActual);
    }
    private void calcularSolucionVorazInicial(){
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
                    distanciaActual = distancias[ciudadActual][i];
                    if(distanciaActual < distanciaMin){
                        distanciaMin = distanciaActual;
                        indexCoordenadaMin = i;
                    }
                }
            }
            this.solucionInicial.add(indexCoordenadaMin);
            ciudadesRecorridas.add(indexCoordenadaMin);
            distanciaMin = Integer.MAX_VALUE;
            ciudadActual = indexCoordenadaMin;
        }
    }
    private void calcularSolucionVoraz(){
        //Generar solucion actual greedy cogiendo la siguiente ciudad que esté más cerca con la matriz de nuevas distancias
        int indexCoordenadaMin = 0;
        int distanciaMin = Integer.MAX_VALUE;
        int distanciaActual;
        int ciudadActual = 0;
        ArrayList<Integer> ciudadesRecorridas = new ArrayList<>();

        ciudadesRecorridas.add(0);
        for(int k = 1; k < coordenadas.length; k++){
            for(int i = 0; i < coordenadas.length; i++){
                if(!ciudadesRecorridas.contains(i)){
                    distanciaActual = nuevasDistancias[ciudadActual][i];
                    if(distanciaActual < distanciaMin){
                        distanciaMin = distanciaActual;
                        indexCoordenadaMin = i;
                    }
                }
            }
            this.solucionActual.add(indexCoordenadaMin);
            ciudadesRecorridas.add(indexCoordenadaMin);
            distanciaMin = Integer.MAX_VALUE;
            ciudadActual = indexCoordenadaMin;
        }
    }
    public void calcularDistancias(){
        int currentMin = 0, currentMax = 0;

        for(int i = 0; i < coordenadas.length; i++){
            for(int j = 0; j < coordenadas.length; j++){
                this.distancias[i][j] = (int)coordenadas[i].distancia(coordenadas[j]);
                currentMin = this.distancias[i][j];
                currentMax = this.distancias[i][j];
                if(currentMax > dMax){
                    dMax = currentMax;
                }
                if(currentMin < dMin && i != j){
                    dMin = currentMin;
                }
            }
        }
    }

    private int maxFrecuencia(){
        int maxFrecuencia = 0, currentFrec;
        for(int i = 0; i < coordenadas.length; i++){
            for(int j = 0; j < coordenadas.length; j++){
                currentFrec = this.frecuencias[i][j];
                if(currentFrec > maxFrecuencia){
                    maxFrecuencia = currentFrec;
                }
            }
        }
        return maxFrecuencia;
    }

    private void calcularNuevasDistancias(){
        double mu = 1;

        for(int i = 0; i < coordenadas.length; i++){
            for(int j = 0; j < coordenadas.length; j++){
                nuevasDistancias[i][j] = distancias[i][j] + (int)mu*(this.dMax - this.dMin)*(frecuencias[i][j]/this.maxFrecuencia());
            }
        }
    }

    private void actualizarFrecuencias(ArrayList<Integer> mejor){
        for(int i = 0; i < mejor.size() - 1; i++){
            frecuencias[mejor.get(i)][mejor.get(i+1)]++;
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
            actualizarFrecuencias(mejorVecino);
            if (costeMejorVecino < costeSolucionActual) {
                this.solucionActual.clear();
                this.solucionActual.addAll(mejorVecino);
                this.costeSolucionActual = costeMejorVecino;
                this.mejorSolucion.clear();
                this.mejorSolucion.addAll(mejorVecino);
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
                solucionActual.clear();
                this.calcularNuevasDistancias();
                this.calcularSolucionVoraz();
                System.out.print("\tRECORRIDO: ");

                for (int i = 0; i < solucionActual.size(); i++) {
                    System.out.print(solucionActual.get(i) + " ");
                }
                frecuencias = new int[coordenadas.length][coordenadas.length];
                System.out.println("\n***************");
                System.out.println("REINICIO: " + reinicio);
                System.out.println("***************");
                noMejora = 0;
                listaTabu.clear();
                ultimoMejor.clear();
                mejora = true;
            }
        }

        //Imprimimos mejor solucion
        System.out.println("\nMEJOR SOLUCION:");
        System.out.print("\tRECORRIDO: ");

        for (int i = 0; i < mejorSolucion.size(); i++) {
            System.out.print(mejorSolucion.get(i) + " ");
        }

        System.out.println("\n\tCOSTE (km): " + this.calcularCosteSolucionActual(mejorSolucion));
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
            coste += distancias[solucion.get(i)][solucion.get(i + 1)];
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
