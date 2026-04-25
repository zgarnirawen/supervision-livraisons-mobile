package com.supervision.livraisons.dto;

public class StatsDuJourDTO {

    private long totalLivraisons;
    private long livrees;
    private long enCours;
    private long ajournees;
    private double tauxSucces;
    private java.util.List<StatsLivreurDTO> parLivreur;
    private java.util.List<StatsCategorieDTO> parCategorie;

    public StatsDuJourDTO() {}

    public static class StatsLivreurDTO {
        private Integer livreurId;
        private String livreurNomComplet;
        private long total;
        private long livrees;
        private long enCours;
        private long ajournees;

        public StatsLivreurDTO() {}
        public StatsLivreurDTO(Integer livreurId, String livreurNomComplet,
                               long total, long livrees, long enCours, long ajournees) {
            this.livreurId = livreurId;
            this.livreurNomComplet = livreurNomComplet;
            this.total = total;
            this.livrees = livrees;
            this.enCours = enCours;
            this.ajournees = ajournees;
        }

        public Integer getLivreurId() { return livreurId; }
        public void setLivreurId(Integer livreurId) { this.livreurId = livreurId; }
        public String getLivreurNomComplet() { return livreurNomComplet; }
        public void setLivreurNomComplet(String livreurNomComplet) { this.livreurNomComplet = livreurNomComplet; }
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public long getLivrees() { return livrees; }
        public void setLivrees(long livrees) { this.livrees = livrees; }
        public long getEnCours() { return enCours; }
        public void setEnCours(long enCours) { this.enCours = enCours; }
        public long getAjournees() { return ajournees; }
        public void setAjournees(long ajournees) { this.ajournees = ajournees; }
    }

    public static class StatsCategorieDTO {
        private String categorie;
        private long total;

        public StatsCategorieDTO() {}
        public StatsCategorieDTO(String categorie, long total) {
            this.categorie = categorie;
            this.total = total;
        }

        public String getCategorie() { return categorie; }
        public void setCategorie(String categorie) { this.categorie = categorie; }
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
    }

    // Getters & Setters
    public long getTotalLivraisons() { return totalLivraisons; }
    public void setTotalLivraisons(long totalLivraisons) { this.totalLivraisons = totalLivraisons; }
    public long getLivrees() { return livrees; }
    public void setLivrees(long livrees) { this.livrees = livrees; }
    public long getEnCours() { return enCours; }
    public void setEnCours(long enCours) { this.enCours = enCours; }
    public long getAjournees() { return ajournees; }
    public void setAjournees(long ajournees) { this.ajournees = ajournees; }
    public double getTauxSucces() { return tauxSucces; }
    public void setTauxSucces(double tauxSucces) { this.tauxSucces = tauxSucces; }
    public java.util.List<StatsLivreurDTO> getParLivreur() { return parLivreur; }
    public void setParLivreur(java.util.List<StatsLivreurDTO> parLivreur) { this.parLivreur = parLivreur; }
    public java.util.List<StatsCategorieDTO> getParCategorie() { return parCategorie; }
    public void setParCategorie(java.util.List<StatsCategorieDTO> parCategorie) { this.parCategorie = parCategorie; }
}
