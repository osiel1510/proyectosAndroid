package com.example.test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CustomDrawingView extends View {
    private String[] arreglo;
    private int posicionArreglo;

    public CustomDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        arreglo = new String[]{};
        posicionArreglo = 0;
    }

    public void setArreglo(String[] nuevoArreglo) {
        arreglo = nuevoArreglo;
        invalidate();
    }

    public void setPosicionArreglo(int nuevaPosicion) {
        posicionArreglo = nuevaPosicion;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float x = 10; // Posición de la primera caja
        Paint paint = new Paint();
        paint.setTextSize(40);
        paint.setColor(0xFF000000); // Color de trazo negro
        paint.setStyle(Paint.Style.STROKE); // Establece el estilo de trazo sin relleno
        int width = getWidth();

        for (String elemento : arreglo) {
            canvas.drawRect(x, 320, x + 79, 400, paint); // Dibuja el rectángulo sin relleno
            if (!elemento.equals(" ")) {
                paint.setStyle(Paint.Style.FILL); // Restablece el estilo de trazo a relleno
                canvas.drawText(elemento, x + 34, 370, paint); // Dibuja el contenido con relleno
                paint.setStyle(Paint.Style.STROKE); // Vuelve a establecer el estilo de trazo sin relleno
            }
            x += 79; // Incrementa el ancho de la caja
        }

        float posicionArregloWidth = 10 + (posicionArreglo * 79);

        paint.setStrokeWidth(5);
        canvas.drawLine(posicionArregloWidth + 40, 200, posicionArregloWidth + 40, 300, paint); // Línea vertical
        canvas.drawLine(posicionArregloWidth + 40, 300, posicionArregloWidth + 20, 280, paint); // Flecha izquierda
        canvas.drawLine(posicionArregloWidth + 40, 300, posicionArregloWidth + 60, 280, paint); // Flecha derecha
    }
}
