import 'package:flutter/material.dart';

import '../../app/theme.dart';

class UserAvatar extends StatelessWidget {
  final String username;
  final double size;
  const UserAvatar({super.key, required this.username, this.size = 44});

  @override
  Widget build(BuildContext context) {
    final color = avatarColorFor(username);
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        color: color,
        shape: BoxShape.circle,
      ),
      alignment: Alignment.center,
      child: Text(
        avatarInitials(username),
        style: TextStyle(
          color: Colors.white,
          fontSize: size * 0.4,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}
