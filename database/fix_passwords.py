import psycopg2

NEW_HASH = "$2b$12$DmS/u0K0ar7Esu5oBINSueBPRYIylnJRNr/94ebaQMJEu5BUfjbFS"

conn = psycopg2.connect(host="localhost", dbname="livraisons_db", user="postgres", password="postgres")
cur = conn.cursor()

cur.execute("UPDATE personnel_mobile SET mot_passe = %s", (NEW_HASH,))
conn.commit()

cur.execute("SELECT idpers, login, LEFT(mot_passe, 20) FROM personnel_mobile")
for row in cur.fetchall():
    print(f"  id={row[0]}  login={row[1]}  hash_prefix={row[2]}")

conn.close()
print("\n✅ Mots de passe mis à jour ! Utilisez: login/password")
print("   sami.b  / password  (Livreur)")
print("   leila.t / password  (Livreur)")
print("   ali.bs  / password  (Contrôleur)")
