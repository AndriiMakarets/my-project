package pack;

import java.util.Objects;

public class ProductFilter {

    private Integer id;
    private String name;
    private Double priceFrom;
    private Double priceTo;
    private Double amountFrom;
    private Double amountTo;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPriceFrom() {
        return priceFrom;
    }

    public void setPriceFrom(Double priceFrom) {
        this.priceFrom = priceFrom;
    }

    public Double getPriceTo() {
        return priceTo;
    }

    public void setPriceTo(Double priceTo) {
        this.priceTo = priceTo;
    }

    public Double getAmountFrom() {
        return amountFrom;
    }

    public void setAmountFrom(Double amountFrom) {
        this.amountFrom = amountFrom;
    }

    public Double getAmountTo() {
        return amountTo;
    }

    public void setAmountTo(Double amountTo) {
        this.amountTo = amountTo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductFilter that = (ProductFilter) o;
        return Objects.equals(name, that.name) && Objects.equals(priceFrom, that.priceFrom) && Objects.equals(priceTo, that.priceTo) && Objects.equals(amountFrom, that.amountFrom) && Objects.equals(amountTo, that.amountTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, priceFrom, priceTo, amountFrom, amountTo);
    }
}
