import psycopg2
from datetime import date, timedelta

def seed_future_data():
    conn = psycopg2.connect(
        host='localhost',
        dbname='livraisons_db',
        user='postgres',
        password='postgres'
    )
    cur = conn.cursor()
    
    # Dates à insérer
    dates = [date(2026, 4, 26), date(2026, 4, 27), date(2026, 4, 28)]
    
    # Récupérer les vrais noms des livreurs
    cur.execute("SELECT idpers, nompers, prenompers, telpers FROM personnel_mobile")
    livreurs_data = {row[0]: row for row in cur.fetchall()}
    livreurs_ids = list(livreurs_data.keys())
    
    villes = ['Tunis', 'Sfax', 'Sousse', 'Bizerte', 'Nabeul']
    clients = [
        ('Sami Gharbi', 'Rue de la Liberté', 'Tunis'),
        ('Amira Ben Salem', 'Avenue Habib Bourguiba', 'Tunis'),
        ('Mohamed Trabelsi', 'Route de Gremda', 'Sfax'),
        ('Mouna Rezgui', 'Sahloul 4', 'Sousse'),
        ('Hassen Mahmoudi', 'Ziatine', 'Tunis'),
        ('Fatma Baklouti', 'Route de Tunis', 'Sfax'),
        ('Ali Mansour', 'Kantaoui', 'Sousse'),
        ('Sarra Jelassi', 'Bizerte Nord', 'Bizerte'),
        ('Walid Toumi', 'Hammamet Sud', 'Nabeul'),
        ('Ines Dridi', 'Manar 2', 'Tunis')
    ]
    
    nocde_start = 2000
    
    for d in dates:
        print(f"Insertion des livraisons pour le {d}...")
        for i, (client_nom, client_adr, client_ville) in enumerate(clients):
            nocde = nocde_start + (d.day * 100) + i
            livreur_id = livreurs_ids[i % len(livreurs_ids)]
            l_info = livreurs_data[livreur_id]
            
            # Insert Livraison
            cur.execute("""
                INSERT INTO livraisons_mobile (nocde, dateliv, livreur_id, livreur_nom, livreur_prenom, livreur_tel, client_nom, client_prenom, client_adresse, client_ville, client_tel, etatliv, modepay)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT (nocde) DO NOTHING
            """, (
                nocde, d, livreur_id, 
                l_info[1], l_info[2], l_info[3],
                client_nom, 'ClientP', client_adr, client_ville,
                f"22000{nocde % 1000:03d}",
                'EC',
                'apres_livraison' if i % 2 == 0 else 'avant_livraison'
            ))
            
            # Insert dummy articles for this delivery
            cur.execute("""
                INSERT INTO articles_commande (nocde, refart, designation, quantite, prix_unitaire)
                VALUES (%s, %s, %s, %s, %s)
                ON CONFLICT DO NOTHING
            """, (nocde, 'A001', 'Stylo Luxe', 10, 1.5))
            
            cur.execute("""
                INSERT INTO articles_commande (nocde, refart, designation, quantite, prix_unitaire)
                VALUES (%s, %s, %s, %s, %s)
                ON CONFLICT DO NOTHING
            """, (nocde, 'A005', 'Marqueur Pro', 5, 2.5))
            
    conn.commit()
    cur.close()
    conn.close()
    print("Données insérées avec succès !")

if __name__ == "__main__":
    seed_future_data()
