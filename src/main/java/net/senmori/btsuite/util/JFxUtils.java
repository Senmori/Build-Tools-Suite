/*
 * Copyright (c) 2018, Senmori. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * The name of the author may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package net.senmori.btsuite.util;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * The Class JFxUtils.
 * Found at: https://gist.github.com/837/1c4037901d3762d5c560 <-- Credits to that person
 */
public class JFxUtils {

    /**
     * Adds automatic scrolling to last index in a table.
     *
     * @param <S>  the generic type
     * @param view the view
     */
    public static <S> void addAutoScrollToTableView(final TableView<S> view) {
        if ( view == null ) {
            throw new NullPointerException();
        }

        view.getItems().addListener( ( ListChangeListener<S> ) ( c ->
        {
            c.next();
            final int size = view.getItems().size();
            if ( size > 0 ) {
                view.scrollTo( size - 1 );
            }
        } ) );
    }


    /**
     * Adds the filter_ windows explorer conform.
     *
     * @param field the field
     */
    public static void addFilter_WindowsExplorerConform(TextField field) {
        field.addEventFilter( KeyEvent.ANY, keyEvent ->
        {
            if ( ! keyEvent.getCharacter().matches( "^[a-zA-Z0-9_]*$" ) && ! keyEvent.getCode().equals( KeyCode.BACK_SPACE ) ) {
                keyEvent.consume();
            }
        } );
    }

    /**
     * Adds the filter_ only numbers.
     *
     * @param field the field
     */
    public static void addFilter_OnlyNumbers(TextField field) {
        field.addEventFilter( KeyEvent.ANY, keyEvent ->
        {
            if ( ! keyEvent.getCharacter().matches( "^[0-9]*$" ) && ! keyEvent.getCode().equals( KeyCode.BACK_SPACE ) ) {
                keyEvent.consume();
            }
        } );
    }

    /**
     * Adds the filter_ only alphanumeric.
     *
     * @param field the field
     */
    public static void addFilter_OnlyAlphanumeric(TextField field) {
        field.addEventFilter( KeyEvent.ANY, keyEvent ->
        {
            if ( ! keyEvent.getCharacter().matches( "^[\\p{L}0-9]*$" ) && ! keyEvent.getCode().equals( KeyCode.BACK_SPACE ) ) {
                keyEvent.consume();
            }
        } );
    }

    /**
     * Adds the filter_ only alphabet.
     *
     * @param field the field
     */
    public static void addFilter_OnlyAlphabet(TextField field) {
        field.addEventFilter( KeyEvent.ANY, keyEvent ->
        {
            if ( ! keyEvent.getCharacter().matches( "\\A[^\\W\\d_]+\\z" ) && ! keyEvent.getCode().equals( KeyCode.BACK_SPACE ) ) {
                keyEvent.consume();
            }
        } );
    }


    /**
     * Force list refresh on.
     *
     * @param <T> the generic type
     * @param lsv the lsv
     */
    public static <T> void forceListRefreshOn(ListView<T> lsv) {
        ObservableList<T> items = lsv.<T> getItems();
        lsv.<T> setItems( null );
        lsv.<T> setItems( items );
    }

    /**
     * Force table view refresh on.
     *
     * @param <T> the generic type
     * @param tbv the tbv
     */
    public static <T> void forceTableViewRefreshOn(TableView<T> tbv) {
        ObservableList<T> items = tbv.<T> getItems();
        tbv.<T> setItems( null );
        tbv.<T> setItems( items );
    }


    /**
     * Creates the error alert.
     *
     * @param e           the error
     * @param contentText the content text
     */
    public static void createErrorAlert(Exception e, String contentText) {
        Alert alert = new Alert( AlertType.ERROR );
        alert.setTitle( "Exception caught" );
        alert.setHeaderText( "There was an Error" );
        alert.setContentText( contentText );

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );
        e.printStackTrace( pw );
        String exceptionText = sw.toString();

        Label label = new Label( "The exception stacktrace was:" );

        TextArea textArea = new TextArea( exceptionText );
        textArea.setEditable( false );
        textArea.setWrapText( true );

        textArea.setMaxWidth( Double.MAX_VALUE );
        textArea.setMaxHeight( Double.MAX_VALUE );
        GridPane.setVgrow( textArea, Priority.ALWAYS );
        GridPane.setHgrow( textArea, Priority.ALWAYS );

        GridPane expContent = new GridPane();
        expContent.setMaxWidth( Double.MAX_VALUE );
        expContent.add( label, 0, 0 );
        expContent.add( textArea, 0, 1 );

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent( expContent );

        alert.showAndWait();
    }


    /**
     * Creates the textbox alert.
     *
     * @param title       the title
     * @param header      the header
     * @param contentText the content text
     * @param textbox     the textbox
     */
    public static void createTextboxAlert(String title, String header, String contentText, String textbox) {
        Alert alert = new Alert( AlertType.INFORMATION );
        alert.setTitle( title );
        alert.setHeaderText( header );
        alert.setContentText( contentText );

        TextArea textArea = new TextArea( textbox );
        textArea.setEditable( false );
        textArea.setWrapText( true );

        textArea.setMaxWidth( Double.MAX_VALUE );
        textArea.setMaxHeight( Double.MAX_VALUE );
        GridPane.setVgrow( textArea, Priority.ALWAYS );
        GridPane.setHgrow( textArea, Priority.ALWAYS );

        GridPane expContent = new GridPane();
        expContent.setMaxWidth( Double.MAX_VALUE );
        expContent.add( textArea, 0, 0 );

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent( expContent );

        alert.showAndWait();
    }

    /**
     * Creates an alert.
     *
     * @param title       the title
     * @param header      the header
     * @param contentText the content text
     */
    public static void createAlert(String title, String header, String contentText) {
        Alert alert = new Alert( AlertType.INFORMATION );
        alert.setTitle( title );
        alert.setHeaderText( header );
        alert.setContentText( contentText );
        alert.showAndWait();
    }

    /**
     * Creates the error alert j unit.
     *
     * @param exception   the exception
     * @param contentText the content text
     */
    public static void createErrorAlertJUnit(Throwable exception, String contentText) {
        Alert alert = new Alert( AlertType.ERROR );
        alert.setTitle( "Test failed" );
        alert.setHeaderText( "This test failed, further details below." );
        alert.setContentText( contentText );

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );
        exception.printStackTrace( pw );
        String exceptionText = sw.toString();

        Label label = new Label( "The exception stacktrace was:" );

        TextArea textArea = new TextArea( exceptionText );
        textArea.setEditable( false );
        textArea.setWrapText( true );

        textArea.setMaxWidth( Double.MAX_VALUE );
        textArea.setMaxHeight( Double.MAX_VALUE );
        GridPane.setVgrow( textArea, Priority.ALWAYS );
        GridPane.setHgrow( textArea, Priority.ALWAYS );

        GridPane expContent = new GridPane();
        expContent.setMaxWidth( Double.MAX_VALUE );
        expContent.add( label, 0, 0 );
        expContent.add( textArea, 0, 1 );

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent( expContent );
        alert.showAndWait();
    }
}
