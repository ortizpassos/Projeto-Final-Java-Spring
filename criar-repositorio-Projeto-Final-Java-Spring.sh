cd "C:\Devs2Blu\Aulas_Ranyer\Projeto-Final-Java-Spring"
git init
git add .
git commit -m "Criação do repositório"
git remote add origin https://github.com/ortizpassos/Projeto-Final-Java-Spring.git
gh repo create Projeto-Final-Java-Spring --public --source=. --push --description "Projetos com Projeto-Final-Java-Spring"
git branch -M main
git push -u origin main