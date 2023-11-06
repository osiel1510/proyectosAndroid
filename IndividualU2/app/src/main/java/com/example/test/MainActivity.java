package com.example.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    String inputAnterior;
    int contadorShot = 0;
    boolean returnProgama = false;
    ArrayList<String> arregloPrincipal = new ArrayList<>();
    int palabraActual = 0;
    int ultimoElemento = 0;
    MyEditText input;
    Button btnStart;
    ArrayList<ArrayList<String>> programa = new ArrayList<ArrayList<String>>();
    List<String> reservedWords = new ArrayList<>();
    List<String> actions = new ArrayList<>();
    ArrayList<Object[]> programaI = new ArrayList<>();
    int posicionArregloActual;
    EditText inputEntrada;
    TimerTask timerTask;
    Timer timer;
    Object[] palabraActual2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inicializar interfaz
        input = findViewById(R.id.input);
        btnStart = findViewById(R.id.btnStart);
        inputEntrada = findViewById(R.id.inputEntrada);

        posicionArregloActual = 0;

        //Listeners
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cargarPrograma();
            }
        });

        //Inicializar variables
        actions.add("Return");
        actions.add("Write");
        actions.add("Move");
        actions.add("Goto");

        reservedWords.add("If");
        reservedWords.add("Blank");
        reservedWords.add("Return");
        reservedWords.add("False");
        reservedWords.add("True");
        reservedWords.add("Write");
        reservedWords.add("Not");
        reservedWords.add("Move");
        reservedWords.add("Right");
        reservedWords.add("Move");
        reservedWords.add("Left");
        reservedWords.add("Goto");
    }

    private int cargarPrograma(){
        contadorShot = 0;
        returnProgama = false;
        arregloPrincipal.clear();
        palabraActual = 0;
        ultimoElemento = 0;
        programa.clear();
        programaI.clear();

        String texto = input.getText().toString();
        texto = texto.replaceAll("(?m)^[ \t]*\r?\n", "");
        input.setText(texto);

        //Eliminar espacios en blanco
        texto = texto.trim();
        input.setText(texto);

        if (texto.replaceAll(" ", "").isEmpty()) {
            return -1;
        }

        String[] lineas = texto.split("\n");

        //Obtener cada arreglo de linea en arrego de palabras
        for (String linea : lineas) {
            String[] palabras = linea.split(" ");
            ArrayList<String> lineaPalabras = new ArrayList<String>(Arrays.asList(palabras));
            programa.add(lineaPalabras);
        }

        //Obtener todas las palabras en un arreglo, su linea y su número de palabra.
        int contador = 0;

        for (int indexLine = 0; indexLine < programa.size(); indexLine++) {
            ArrayList<String> line = programa.get(indexLine);

            for (String word : line) {
                programaI.add(new Object[] { word, indexLine, contador });
                contador++;
            }
        }

        // Obtener la última línea
        int ultimaLinea = (int) programaI.get(programaI.size() - 1)[1];

        // Obtener el último elemento
        int ultimoElemento = programaI.size();

        for (int i = ultimoElemento; i < ultimoElemento + 10; i++) {
            programaI.add(new Object[] { "        ", ultimaLinea, i });
        }

        List<Object> valor = verifyLanguage(programa, reservedWords);

        if (programaI.get(programaI.size() - 12)[0].toString().trim().equals("Goto") || programaI.get(programaI.size() - 12)[0].toString().trim().equals("Return")) {
        } else {
            valor = Arrays.asList("Error en la última línea: El programa debe terminar con una instrucción Return o Goto", ultimaLinea);
        }

        if (valor == null){
            valor = verifySintaxis();
            if(valor == null){
                correrPrograma();
            }else{
                Toast.makeText(MainActivity.this, valor.get(0).toString(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(MainActivity.this, valor.get(0).toString(), Toast.LENGTH_LONG).show();
        }

        return 0;

    }
    public List<Object> verifySintaxis() {
        int indice = 0;
        while (indice < programaI.size()) {

            Object[] word = programaI.get(indice);

            if ("If".equals(word[0].toString().trim())) { // Verificar sentencia if
                if ("Not".equals(programaI.get(indice + 1)[0])) {
                    List<Object> result = verifyIf(programaI.subList(indice, indice + 5));
                    if (!(boolean) result.get(0)) {
                        return Arrays.asList(result.get(1));
                    } else {
                        indice += 4;
                    }
                } else {
                    List<Object> result = verifyIf(programaI.subList(indice, indice + 4));
                    if (!(boolean) result.get(0)) {
                        return Arrays.asList(result.get(1));
                    } else {
                        indice += 3;
                    }
                }
            } else if (actions.contains(word[0].toString().trim())) { // Verificar acciones
                List<Object> result = verifyAction(programaI.subList(indice, indice + 2));
                if (!(boolean) result.get(0)) {
                    return Arrays.asList("Error, instrucción incompleta en la línea: " + word[1] + ", " + word[0].toString().trim(), word[1]);
                } else {
                    indice += 1;
                }
            } else if (isCharacter(word[0].toString().trim()) || "Blank".equals(word[0].toString().trim())) { // Verificar carácter
                return Arrays.asList("Error, caracter \"" + word[0].toString().trim() + "\" en posición incorrecta, línea: " + word[1], word[1]);
            } else if (isFunction(word[0].toString().trim()) && isCallFunction(word[0].toString().trim())) { // Verificar si es un tag
                return Arrays.asList("Error, etiqueta \"" + word[0].toString().trim() + "\" en posición incorrecta, línea: " + word[1], word[1]);
            }

            if (isFunction(word[0].toString().trim()) && isFunction(programaI.get(indice + 1)[0].toString())) {
                if (!isCallFunction(word[0].toString().trim()) && !isCallFunction(programaI.get(indice + 1)[0].toString())) {
                    return Arrays.asList("Error, etiqueta vacía \"" + word[0].toString().trim() + "\" en la línea: " + word[1], word[1]);
                }
            }

            indice++;
        }

        return null; // No se encontraron errores de sintaxis
    }
    public void actualizarArregloWidget() {
        CustomDrawingView customView = findViewById(R.id.customDrawingView);
        customView.setArreglo(arregloPrincipal.toArray(new String[0])); // Convierte ArrayList a String[]
        customView.setPosicionArreglo(posicionArregloActual);

        int nuevoAncho = 10 + (arregloPrincipal.size() * 79);
        customView.setMinimumWidth(nuevoAncho);

        int nuevaPosicion = 10 + (posicionArregloActual * 79);
        HorizontalScrollView scrollArea = findViewById(R.id.horizontalScrollview);
        scrollArea.setScrollX(nuevaPosicion);
    }
    private void correrPrograma() {
        String entrada = inputEntrada.getText().toString();

        if (entrada.isEmpty()) {
            //labelErrores.setText("¡Ingresa al menos un valor en la entrada!"); // Verificación de que ingresen al menos uno
            return;
        }

        for(int i = 0; i < entrada.length(); i++){
            arregloPrincipal.add(String.valueOf(entrada.charAt(i)));
        }
        // Inicialización de las variables
        //labelErrores.setText("");
        palabraActual2 = programaI.get(0);
        posicionArregloActual = 0;
        actualizarArregloWidget();
        ultimoElemento = programaI.size() - 9;

        timer = new Timer();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        animarPrograma();
                    }
                });
            }
        };

        inputAnterior = input.getText().toString();

        timer.schedule(timerTask, 0, 600);
    }
    private void animarPrograma() {
        if (Integer.valueOf(palabraActual2[2].toString().trim()) > ultimoElemento) {
            timer.cancel();
            // En caso de que se llegue al final del programa
        }

        //colorearLinea(palabraActual2[1], "yellow"); // Marcar la línea en la que va
        //colocarCursor(palabraActual2[1]); // Mover el cursor a la línea

        int indice = Integer.valueOf(palabraActual2[2].toString()); // Obtener el índice de la palabra
        int indiceInicial = Integer.valueOf(palabraActual2[2].toString()); // Respaldar el índice

        Object[] word = palabraActual2; // Actualizar la variable word

        if (word[0].toString().trim().equals("If")) { // En caso de ser sentencia if
            if (programaI.get(indice + 1)[0].toString().trim().equals("Not")) {
                ejecutarIf(programaI.subList(indice, indice + 5));
                indice += 4; // Actualizar índice para saltar las palabras ya utilizadas
            } else {
                ejecutarIf(programaI.subList(indice, indice + 4));
                indice += 3; // Actualizar índice para saltar las palabras ya utilizadas
            }
        } else if (actions.contains(word[0].toString().trim())) { // En caso de que sea una acción
            ejecutarAction(programaI.subList(indice, indice + 2));
            indice += 1; // Actualizar índice para saltar las palabras ya utilizadas
        }

        indice += 1; // Mover al siguiente índice
        if (indiceInicial == Integer.valueOf(palabraActual2[2].toString().trim())) { // Actualizar la palabra actual
            palabraActual2 = programaI.get(indice);
        }

        contadorShot++;
        actualizarArregloWidget();
    }
    private void ejecutarIf(List<Object[]> words) {
        if (words.get(0)[0].toString().trim().equals("If")) {
            if (words.get(1)[0].toString().trim().equals("Not")) {
                if (words.get(2)[0].toString().trim().equals("Blank")) {
                    if (!arregloPrincipal.get(posicionArregloActual).equals(" ")) {
                        ejecutarAction(words.subList(3, 5));
                    }
                } else {
                    if (!arregloPrincipal.get(posicionArregloActual).equals(String.valueOf(words.get(2)[0].toString().trim().charAt(1)))) {
                        ejecutarAction(words.subList(3, 5));
                    }
                }
            } else {
                System.out.println("if without blank");
                if (words.get(1)[0].toString().trim().equals("Blank")) {
                    if (arregloPrincipal.get(posicionArregloActual).equals(" ")) {
                        ejecutarAction(words.subList(2, 4));
                    }
                } else {
                    if (arregloPrincipal.get(posicionArregloActual).equals(String.valueOf(words.get(1)[0].toString().trim().charAt(1)))) {
                        ejecutarAction(words.subList(2, 4));
                    }
                }
            }
        }
    }
    private void ejecutarAction(List<Object[]> words) {
        if (words.get(0)[0].toString().trim().equals("Goto")) { // Actualización de la palabra actual para hacer match con la etiqueta
            String palabra = words.get(1)[0].toString().trim() + ":";
            for (Object[] i : programaI) {
                if (i[0].toString().trim().equals(palabra)) {
                    palabraActual2 = i;
                }
            }
        } else if (words.get(0)[0].toString().trim().equals("Write")) { // Cambiar el valor de la cabeza
            String palabra;
            if (words.get(1)[0].toString().trim().equals("Blank")) {
                palabra = " ";
            } else {
                palabra = String.valueOf(words.get(1)[0].toString().trim().charAt(1));
            }
            arregloPrincipal.set(posicionArregloActual, palabra);
        } else if (words.get(0)[0].toString().trim().equals("Return")) { // Finalizar el programa con un return
            //returnPrograma = words.get(1)[0];
            timer.cancel();
            //buttonContinue.setEnabled(false);
            if (words.get(1)[0].toString().trim().equals("True")) { // Dependiendo del true or false, pintar la línea de cierto color
                //colorearLinea(palabraActual[1], Color.rgb(0, 230, 0));
                //colocarCursor(palabraActual[1]);
            } else {
                //colorearLinea(palabraActual[1], Color.RED);
                //colocarCursor(palabraActual[1]);
            }
        } else if (words.get(0)[0].toString().trim().equals("Move")) { // Mover la cabeza
            if (words.get(1)[0].toString().trim().equals("Right")) {
                if (posicionArregloActual == arregloPrincipal.size() - 1) {
                    arregloPrincipal.add(" ");
                }
                posicionArregloActual++;
            } else {
                if (posicionArregloActual == 0) {
                    arregloPrincipal.add(0, " ");
                } else {
                    posicionArregloActual--;
                }
            }
        }
    }
    private List<Object> verifyLanguage(List<ArrayList<String>> programa, List<String> reservedWords) {
        List<String> funciones = new ArrayList<>();
        if (!programa.get(0).get(0).equals("Start:")) {
            return Arrays.asList("Error en la línea 0: El programa debe iniciar con la etiqueta Start", 0);
        }

        for (int indexLine = 0; indexLine < programa.size(); indexLine++) {
            //Iterar en las lineas del programa
            ArrayList<String> line = programa.get(indexLine);
            for (String wordWithSpaces : line) {
                String word = wordWithSpaces.trim();
                if (reservedWords.contains(word)) {
                } else {
                    if (isCharacter(word)) {
                    } else {
                        if (!isFunction(word)) {
                            return Arrays.asList("Error en la línea " + (indexLine + 1) + ": Palabra desconocida, " + word, indexLine);
                        } else {
                            if (reservedWords.contains(word.substring(0, word.length() - 1))) {
                                return Arrays.asList("Error en la línea " + (indexLine + 1) + ": Las etiquetas no se pueden llamar como alguna palabra reservada, " + word, indexLine);
                            } else {
                                if (isCallFunction(word)) {
                                    boolean valor = false;
                                    for (ArrayList<String> line2 : programa) {
                                        for (String word2 : line2) {
                                            if ((word + ":").equals(word2)) {
                                                valor = true;
                                            }
                                        }
                                    }
                                    if (!valor) {
                                        return Arrays.asList("Error en la línea " + (indexLine + 1) + ": Etiqueta llamada sin declarar, " + word, indexLine);
                                    }
                                }
                            }
                        }
                    }
                }

                if (isFunction(word)) { // Verificar si la declaración de una etiqueta se repite
                    if (!isCallFunction(word)) {
                        funciones.add(word);
                        if (Collections.frequency(funciones, word) > 1) {
                            return Arrays.asList("Error, etiqueta repetida: " + word, indexLine);
                        }
                    }
                }
            }
        }
        return null; // No se encontraron errores
    }
    public List<Object> verifyIf(List<Object[]> words) {
        if ("If".equals(words.get(0)[0].toString().trim())) {
            if ("Not".equals(words.get(1)[0].toString().trim())) {
                String condition = words.get(2)[0].toString().trim();
                if ("Blank".equals(condition) || isCharacter(condition)) {
                    return verifyAction(words.subList(3, 5));
                } else {
                    return Arrays.asList(false, "Error, instrucción errónea " + condition + " en la línea: " + (Integer.parseInt(words.get(2)[1].toString()) + 1), words.get(2)[1].toString().trim());
                }
            } else {
                String condition = words.get(1)[0].toString().trim();
                if ("Blank".equals(condition) || isCharacter(condition)) {
                    return verifyAction(words.subList(2,4));
                } else {
                    return Arrays.asList(false, "Error, instrucción errónea " + condition + " en la línea: " + (Integer.parseInt(words.get(1)[1].toString()) + 1), words.get(1)[1].toString().trim());
                }
            }
        } else {
            return Arrays.asList(false, "Error, instrucción errónea " + words.get(0)[0].toString().trim() + " en la línea: " + (Integer.parseInt(words.get(0)[1].toString()) + 1), words.get(0)[1].toString().trim());
        }
    }

    public List<Object> verifyAction(List<Object[]> words) {
        String action = words.get(0)[0].toString().trim();



        if ("Goto".equals(action)) {
            return verifyGoto(words);
        } else if ("Write".equals(action)) {
            return verifyWrite(words);
        } else if ("Return".equals(action)) {
            return verifyReturn(words);
        } else if ("Move".equals(action)) {
            String moveDirection = words.get(1)[0].toString().trim();
            if ("Right".equals(moveDirection) || "Left".equals(moveDirection)) {
                return Arrays.asList(true, null);
            } else {
                return Arrays.asList(false, "Error, instrucción errónea " + moveDirection + " en la línea: " + (Integer.parseInt(words.get(1)[1].toString()) + 1), words.get(1)[1]);
            }
        } else {
            return Arrays.asList(false, "Error, instrucción errónea " + action + " en la línea: " + (Integer.parseInt(words.get(0)[1].toString()) + 1), words.get(0)[1]);
        }
    }

    public List<Object> verifyReturn(List<Object[]> words) {
        if ("True".equals(words.get(1)[0].toString().trim()) || "False".equals(words.get(1)[0].toString().trim())) {
            return Arrays.asList(true, null);
        } else {
            return Arrays.asList(false, "Error, instrucción errónea " + words.get(1)[0] + " en la línea: " + words.get(1)[1], words.get(1)[1]);
        }
    }

    public List<Object> verifyWrite(List<Object[]> words) {
        if ("Blank".equals(words.get(1)[0].toString().trim()) || isCharacter(words.get(1)[0].toString().trim())) {
            return Arrays.asList(true, null);
        } else {
            return Arrays.asList(false, "Error, instrucción errónea " + words.get(1)[0] + " en la línea: " + words.get(1)[1], words.get(1)[1]);
        }
    }

    public List<Object> verifyGoto(List<Object[]> words) {
        if (!reservedWords.contains(words.get(1)[0].toString().trim())) {
            return Arrays.asList(true, null);
        } else {
            return Arrays.asList(false, "Error, instrucción errónea " + words.get(1)[0] + " en la línea: " + (Integer.parseInt(words.get(1)[1].toString()) + 1), words.get(1)[1]);
        }
    }

    public boolean isCharacter(String word) { //Verificar si es un caracter
        if (word.length() == 3 && word.charAt(0) == '\'' && word.charAt(2) == '\'') {
            return true;
        } else {
            return false;
        }
    }

    public boolean isFunction(String word) {
        if(word.length() != 0){
            if (Character.isUpperCase(word.charAt(0))) {
                return true;
            } else {
                return false;
            }
        }else{
            return false;
        }
    }

    public boolean isCallFunction(String word) {
        if (word.charAt(word.length() - 1) != ':') {
            return true;
        } else {
            return false;
        }
    }


}