import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

typedef void IntCallback(int val);
class RoleSelectChip extends StatefulWidget {

  RoleSelectChip({Key? key, required this.roles, required this.onSelectedRoleChange}) : super(key: key);

  List<String> roles;
  IntCallback onSelectedRoleChange;

  @override
  _RoleSelectChipState createState() =>
      new _RoleSelectChipState();
}

class _RoleSelectChipState extends State<RoleSelectChip> with TickerProviderStateMixin {

  int _selectedIndex = 0;

  Widget _buildChips() {
    List<Widget> chips = List.empty(growable: true);

    for (int i = 0; i < widget.roles.length; i++) {
      ChoiceChip choiceChip = ChoiceChip(
        selected: _selectedIndex == i,
        label: Text(widget.roles[i], style: TextStyle(color: Colors.white)),
        pressElevation: 5,
        backgroundColor: Colors.black54,
        selectedColor: Colors.blue,
        onSelected: (bool selected) {
          setState(() {
            if (selected) {
              _selectedIndex = i;
              this.widget.onSelectedRoleChange(i);
            }
          });
        },
      );

      chips.add(Padding(
          padding: EdgeInsets.symmetric(horizontal: 2),
          child: choiceChip
      ));
    }

    return Wrap(
      // This next line does the trick.
      children: chips,
    );
  }

  @override
  Widget build(BuildContext context) {
    return _buildChips();
  }
}