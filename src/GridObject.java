public interface GridObject {
    private int rowNum;
    private int colNum;

    @default
    public int getRowNum() {
        return rowNum;
    }

    @default
    public int getColNum() {
        return colNum;
    }
}