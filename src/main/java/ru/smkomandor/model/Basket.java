package ru.smkomandor.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Basket {
    private List<Row> rows;
    private Integer count;
    private BigDecimal amount;
    private static Basket basket;

    private Basket() {
        rows = new ArrayList<>();
        count = 0;
        amount = BigDecimal.valueOf(0);
    }

    public static Basket get() {
        if (basket == null) {
            basket = new Basket();
        }
        return basket;
    }

    public List<Row> getRows() {
        return rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public static class Row {
        private Integer id;
        private Integer lineNumber;
        private String name;
        private BigDecimal cost;
        private Integer count;
        private BigDecimal lineAmount;

        public Row() {
        }

        public Row(Integer id, String name, BigDecimal cost, Integer lineNumber, Integer count, BigDecimal lineAmount) {
            this.id = id;
            this.name = name;
            this.cost = cost;
            this.lineNumber = lineNumber;
            this.count = count;
            this.lineAmount = lineAmount;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getCost() {
            return cost;
        }

        public void setCost(BigDecimal cost) {
            this.cost = cost;
        }

        public Integer getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(Integer lineNumber) {
            this.lineNumber = lineNumber;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public BigDecimal getLineAmount() {
            return lineAmount;
        }

        public void setLineAmount(BigDecimal lineAmount) {
            this.lineAmount = lineAmount;
        }
    }

    public static void add(Good good) {
        List<Row> rows = basket.getRows();
        Optional<Row> optionalRow = rows.stream()
                .filter(row -> good.getName().equals(row.getName()))
                .findAny();
        if (optionalRow.isPresent()) {
            Row row = optionalRow.get();
            row.setCount(row.getCount() + 1);
            row.setLineAmount(row.getLineAmount().add(row.getCost()));
        } else {
            rows.add(new Row(
                            good.getId(),
                            good.getName(),
                            good.getCost(),
                            rows.size() + 1,
                            1,
                            good.getCost()
                    )
            );
        }
        basket.setCount(basket.getCount() + 1);
        basket.setAmount(basket.getAmount().add(good.getCost()));
    }

    public static void delete(Basket.Row row) {
        basket.setCount(basket.getCount() - row.getCount());
        basket.setAmount(basket.getAmount().subtract(row.getLineAmount()));
        List<Row> rows = basket.getRows();
        rows.remove(row.getLineNumber() - 1);
        updatingIndexes();
    }

    public static void clear() {
        basket.getRows().clear();
        basket.setCount(0);
        basket.setAmount(BigDecimal.valueOf(0));
    }
    private static void updatingIndexes() {
        List<Row> rows = basket.getRows();
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setLineNumber(i + 1);
        }
    }
}