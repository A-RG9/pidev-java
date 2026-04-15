filepath = r'c:\Users\lenovo\Desktop\pijava\wellora\src\main\java\com\wellcare\javafx\controller\admin\ProfessionalManagementController.java'
with open(filepath, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('public SimpleBooleanProperty selectedProperty', 'public User getUser() { return user; }\n        public SimpleBooleanProperty selectedProperty')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(text)
print("applied fix")
