
import os

filepath = r'c:\Users\lenovo\Desktop\pijava\wellora\src\main\java\com\wellcare\javafx\controller\admin\UserManagementController.java'

with open(filepath, 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
found = False
for line in lines:
    new_lines.append(line)
    if 'usersList = FXCollections.observableArrayList();' in line and not found:
        # Check if already filtered
        if 'filteredUsers =' not in lines[lines.index(line) + 1]:
            new_lines.append('        filteredUsers = new javafx.collections.transformation.FilteredList<>(usersList, p -> true);\n')
            found = True

with open(filepath, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

print("Applied fix successfully")
