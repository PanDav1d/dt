import 'package:doctag_app/remote_clients/api.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class ListItem {
  int value;
  String name;

  ListItem(this.value, this.name);
}

class SignDocumentView extends StatefulWidget {

  final DoctagDocumentAndPdfFile docAndFile;
  SignDocumentView({Key? key, required this.docAndFile}) : super(key: key);

  @override
  _SignDocumentView createState() => _SignDocumentView();
}

class _SignDocumentView extends State<SignDocumentView> {

  late List<ListItem> _dropdownItems;

  late List<DropdownMenuItem<ListItem>> _dropdownRoleMenuItems;
  late ListItem _selectedRole;

  void initState() {
    super.initState();
    _dropdownItems = widget.docAndFile.document.document?.workflow?.actions?.asMap().entries.map((e) => ListItem(e.key, e.value!.role!)).toList() ?? List.empty();
    _dropdownRoleMenuItems = buildRoleDropDownMenuItems(_dropdownItems);
    _selectedRole = _dropdownRoleMenuItems[0].value!;

  }

  List<DropdownMenuItem<ListItem>> buildRoleDropDownMenuItems(List listItems) {
    List<DropdownMenuItem<ListItem>> items = List.empty(growable: true);
    for (ListItem listItem in listItems) {
      items.add(
        DropdownMenuItem(
          child: Text(listItem.name),
          value: listItem,
        ),
      );
    }
    return items;
  }


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: new AppBar(
        title: new Text("Dokument signieren"),
        actions: <Widget>[
          new IconButton(icon: const Icon(Icons.save), onPressed: () {})
        ],
      ),
      body: new Column(
        children: <Widget>[
          new ListTile(
            leading: const Icon(Icons.category),
            title: DropdownButtonFormField<ListItem>(
                decoration: InputDecoration(
                  labelText: 'Rolle',
                ),
                value: _selectedRole,
                items: _dropdownRoleMenuItems,
                onChanged: (value) {
                  setState(() {
                    _selectedRole = value!;
                  });
                })),
          new ListTile(
            leading: const Icon(Icons.vpn_key),
            title: new TextField(
              decoration: new InputDecoration(
                hintText: "Phone",
              ),
            ),
          ),
          new ListTile(
            leading: const Icon(Icons.email),
            title: new TextField(
              decoration: new InputDecoration(
                hintText: "Email",
              ),
            ),
          ),
          const Divider(
            height: 1.0,
          ),
          new ListTile(
            leading: const Icon(Icons.label),
            title: const Text('Nick'),
            subtitle: const Text('None'),
          ),
          new ListTile(
            leading: const Icon(Icons.today),
            title: const Text('Birthday'),
            subtitle: const Text('February 20, 1980'),
          ),
          new ListTile(
            leading: const Icon(Icons.group),
            title: const Text('Contact group'),
            subtitle: const Text('Not specified'),
          )
        ],
      ),
    );
  }
}