package com.multimediachat.app.im.plugin.xmpp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a set of data results returned as part of a search. The report is structured 
 * in columns and rows.
 * 
 * @author Gaston Dombiak
 */
public class PicaSearchResult {
    
    private List<String> columns = new ArrayList<String>();
    private List<Row> rows = new ArrayList<Row>();
    public PicaSearchResult(){
    	addColumn("username");
    	addColumn("nickname");
    	addColumn("gender");
    	addColumn("region");
    	addColumn("status");
    	addColumn("fbid");
    	addColumn("distance");

    }

    public void addRow(Row row){
        rows.add(row);
    }

    public void addColumn(String column){
        columns.add(column);
    }

    public Collection<Row> getRows() {
        return new ArrayList<Row>(rows);
    }

    public Collection<String> getColumns() {
        return new ArrayList<String>(columns);
    }


    public static class Row {
        private List<Field> fields = new ArrayList<Field>();
        public Row(List<Field> fields) {
            this.fields = fields;
        }
        public Field getValue(int index) {
        	return fields.get(index);
        }
        public String getValue(String col) {
        	for(Field field:fields) {
        		if (field.getVariable().equals(col))
        			return field.getValue();
        	}
        	return null;
        }
        public int getSize() {
        	return fields.size();
        }
        public Collection<Field> getFields(){
        	return fields;
        }
    }

    public static class Field {
        private String variable;
        private String value;

        public Field(String variable, String value) {
            this.variable = variable;
            this.value = value;
        }

        public String getVariable() {
            return variable;
        }

        public String getValue() {
            return value;
        }
    }
}
