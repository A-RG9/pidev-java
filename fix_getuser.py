import os

filepath = r'c:\Users\lenovo\Desktop\pijava\wellora\src\main\java\com\wellcare\javafx\controller\admin\ProfessionalManagementController.java'

with open(filepath, 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
found = False
for line in lines:
    new_lines.append(line)
    if 'public ProfessionalTableItem(User user) {' in line and not found:
        # Check if already added
        if 'getUser' not in line:
            pass # We will add it after the constructor ends
    if 'this.user = user;' in line and not found:
        pass
    if '}' in line and not found:
        # Check previous line
        if 'this.user = user;' in lines[lines.index(line) - 1]:
            new_lines.append('        public User getUser() { return user; }\n')
            found = True

with open(filepath, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

print("Applied fix successfully")
