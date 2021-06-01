import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

typedef void IntCallback(int val);
class KeySelectChip extends StatefulWidget {

  KeySelectChip({Key? key, required this.keys, required this.onSelectedKeyChange}) : super(key: key);

  List<String> keys;
  IntCallback onSelectedKeyChange;

  @override
  _KeySelectChipState createState() =>
      new _KeySelectChipState();
}

class _KeySelectChipState extends State<KeySelectChip> with TickerProviderStateMixin {

  int _selectedIndex = 0;

  Widget _buildChips() {
    List<Widget> chips = List.empty(growable: true);

    for (int i = 0; i < widget.keys.length; i++) {
      ChoiceChip choiceChip = ChoiceChip(
        selected: _selectedIndex == i,
        label: Text(widget.keys[i], style: TextStyle(color: Colors.white)),
        pressElevation: 5,
        backgroundColor: Colors.black54,
        selectedColor: Colors.blue,
        onSelected: (bool selected) {
          setState(() {
            if (selected) {
              _selectedIndex = i;
              this.widget.onSelectedKeyChange(i);
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